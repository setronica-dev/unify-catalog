## Archives

Audit: who, when, and how changed document X?
Content Versioning / History: what was the state of X at _that_ time or before _that_ update? 

These are the questions the archive is usually designed to answer. The questions can be focused to examine a specific
attribute, broadened to explore all changes made by a certain process or user, and whatever business-specific queries
and analytics are necessary -- not unlike a DWH.

These are not questions commonly asked, but an answer is needed, having to involve the dev team and hearing "we'll try
to correlate database backups with logs, come back later" is not an approach business would usually appreciate.

Broadly speaking, tracking changes made to the catalogue is optional -- and can easily eclipse the catalogue itself in
costs -- but there are few alternatives for finding the answer to the questions with, possibly, business and financial
consequences (and, of course, legal). As always, the bounds of what is to be kept, for how long, and how easy it would
be to access, are up to the business requirements, but there should still be a few options to choose from.

### What to store?

#### Object snapshot: the entire domain object, copy of

As obvious a solution as it is costly, storing a full copy of the persistent object (plus metadata about the operation)
every time it is changed is an approach that should be used only when necessary, but is otherwise quite effective, if
not efficient. The archive's storage can be arranged and indexed completely freely, independent of the primary storage
configuration and optimisations.

* **Why**: Easy to implement, maintain, find a previous version, revert to it. Suitable for complex atomic documents.
* **Where**: external storage. This full-document history can rapidly outpace the primary storage's growth, so cheaper,
slower, less accessible storage is better. If possible, partitioning this storage by the modification date would allow
quickly dropping an entire \[month-long] segment when it expires, instead of batch-removing rows one-by-one.
* **Why not**: Overwhelming redundancy and storage cost concerns for large or volatile catalogues.
* **What else**:
  * Ill-suited for objects consisting of multiple nested documents, but as a 'compromise' solution, each such document
  could have an archive of its own. Does not scale well with complexity, so assembling a full snapshot view could take
  considerably more effort.
  * Keeping the archive in the same storage as the primary data, in separate tables, _can_ be reasonable for moderately
  large catalogues. Using read-only replicas to access archives can alleviate some load, but be prepared to remove the
  data to an external storage if the critical path functionality is impacted or storage scaling becomes unreasonable.
  * History (that captures every version of the document, in the correct order) will naturally form a journal. This
  journal can, in theory, serve as a streaming connector between the catalogue core and downstream consumers. However,
  the two systems -- random access long-term storage and ordered messaging -- have requirements and goals that are not
  in alignment with each other.
  * The opposite can be approximated as well -- if the system uses a journal, its entries can hold relevant metadata
  and serve as history storage, albeit with limited random access tools. Ideally, dedicated history storage can then
  be implemented as a consumer of said journal.
  * If the entity is "slowly changing" and the older variants of it still have some relevance[^1], then, by all means,
  keep them in the same table, but that's likely a fundamentally different requirement.

[^1]: I.e. the older, immutable versions of the entity in question can be strongly referenced by other entities within
business logic. See `SchemaDefinition.java` and how it connects to the other two entities in the source for an example.
Products validated by an older schema may be considered 'outdated' and require special handling. 

#### Value snapshot: fixed place, fixed part 

Store the _previous version_ of the column `X` in the same object, in a column `previousX`, plus any necessary metadata
columns. Business-specific columns, like `approvedX` or `prevContractX`, could also exist in the same space and serve a
similar purpose, containing a snapshot of any value of `X`, including current or any past, not just the previous one. 

Like the full-object, the value snapshot can include arbitrary amounts of data, even in a single 'column'. For example:
price. In the wild world of Product Content Management, it may include (and is not limited to): monetary amount and
currency, base quantity (price multiplier that's independent of packaging, for extra-small prices), unit of measure,
package quantity (actual units per purchase), an arbitrarily complex breakdown of pricing tiers or discounts for bulk
or repeat purchases. What parts of that is part of the 'price' entity and what isn't varies with domain and business
requirements, but multiply that by pre-scheduled price changes, shipping costs, B2B contracts and negotiations thereof,
and that's a whole collection of documents to track and capture history for.

This approach is obviously limited in scope (what parts of history are available and how they can be discovered), but
it is still sufficient to satisfy requirements at reasonable cost. Searchable archive is not the only factor, however
-- from the point of view of 'which exact value of X was used for business process Y', saving a(n immutable) snapshot
during that exact process is irreplaceable.

* **Why**: Useful as reference points for certain business processes, has a niche for limited & affordable records.
* **Where**: Anywhere within reason & requirements. Can be in the same table, in a 1:1 table, external, as long as it
  is within the right domain and ownership context[^2].
* **Why not**: Cannot be used for ad lib investigations, scales poorly with increases in complexity.
* **What else**:
  * While combining this approach with the full-object snapshots may be _technically possible_, by referencing the
  relevant row of the full archive in lieu of creating a narrow snapshot, but that risks overcomplicating retention
  logic or losing references. If the value is valuable enough to reference, it's valuable enough to denormalize.
  * The snapshot can and should be stored in whatever documents that are most relevant to the process that required
  the snapshot in the first place, including in multiple places, as needed. A product's name, identifier, and price
  may be stored in a purchase request, with whatever other relevant references to the original entity, then when the
  PR is approved and an order is created from it, the snapshots and references are copied along, probably repeating
  for fulfillments, invoices, etc.

[^2]: If the process or document consists of parts with different ownership[^3], then the snapshot is owned by the
party that executed the relevant business process, regardless of the ownership of the referenced part, and is stored
in the executing party's storage, if applicable. For example, if the part in question if 'price' (owned by 'supplier')
and the process is 'purchase' (owned by 'buyer'), then the 'price' snapshot belongs to the buyer and is likely stored
in their purchase-adjacent documents.

[^3]: A domain object or process can consist of parts that are owned by different parties, exactly one per part. Said
parts are persisted in isolation, with independent lifecycles that are controlled solely by the owner. Current state
of the composite domain entity is evaluated on-demand by combining persistent states of all of its components.

#### Incremental: update delta 

Instead of creating a full copy of the document for each change, record only the differences from the last document
revision. Definitely is much more compact, may be more expensive to compute, is harder to use for some use-cases.
The write-time computational impact varies with the tools available in the primary storage, but it'd likely still be
easier to generate a delta between two snapshots on demand.

* **Why**: A focused, affordable fit for requirements that demand a linear history of changes. Synergistic with delta
  update writers. Enables sparse indexing for changed attributes, compatible with time-based indexing. 
* **Where**: Similar to full snapshots, except the average record is much smaller and is less consistent for indexing. 
* **Why not**: Reconstructing a snapshot is considerably more expensive and requires a full start or end point.
* **What else**:
  * Like full snapshots, the two-way considerations about history and journals apply here, are arguably more relevant,
  if the catalogue uses incremental updates.
  * Combining the snapshot and delta approaches does not necessitate storing both (which remains an option, if the
  combined features outweigh storage concerns -- plus the two histories can use entirely different storage), it could
  be enough to store a part of the delta as (indexable) metadata alongside the snapshot.

#### Raw source feeds 

A step removed from the catalogue's domain entities, archives for incoming feeds serve a different, non-interactive
purpose, but can still serve as a non-definitive audit and backup tool.

* **Why**: Incoming source files are documents and may require retention regardless. That archive may double as an
extremely budget audit datasource, at least if the files are the sole source of changes.
* **Where**: Dedicated file storage. Metadata can be additionally indexed in any DB.
* **Why not**: Incomplete and insufficient. Finding and retrieving specific data is high-effort.

### Exfiltrating the data

Before we can store and read all the archived data, first we need to retrieve and deliver it. There are numerous ways
to achieve that are, of course, dependent on the specific circumstances of a specific catalogue's design and tech. It
should also be said, before we delve into a top-level overview, that if that specific catalogue has requirements that
demand audit or archives, then their inclusion can and should be taken into account when the tech is chosen and design
is made in the first place (if the requirements are added 'too late', then, by all means, compromises must be made).

#### Built-in

Some databases may offer built-in audit or document history. Carefully examine the capabilities and limitations of the
functionality offered before committing to using it (or, indeed, the database itself). Just because the feature exists
does not mean it will be suitable for the requested business purposes (it might not track data changes, not track the
user, or track only the shared DB user instead of business actors), or match the technical expectations (by keeping the
archive in the same DB or use limited or different tools for access).

If it fits and it works? It might just be the best option, starting out or (permanently) temporary.

#### Manual

This, however, is always available, is flexible enough to accommodate any data or pattern required, but demands more
responsibility and effort than most other approaches. There are libraries that streamline the experience somewhat,
but take care to examine the guarantees and caveats they promise.

When executing an update, pass the relevant data and metadata to the repository of choice. If the primary storage is
transactional, and the archive uses the same DB, then submitting both in the same transaction immediately solves most
of the issues (except serialization, more on that later). Transactional primary and external secondary can be reduced
to a previously solved form by introducing an outbox (and eventual consistency with that, but that should hardly be a
surprise with external storage).

Consistency in non-transactional primary storage can be trickier, at least if concurrent writes are possible and likely
_and_ the archive has strict requirements. There are ways to work around / with it, but it might be worth re-evaluating
both the tech choices and the requirements before things get too complicated.

* **Pros**: Complete freedom to use any technology and record any data, with any level of consistency and transparency.
* **Cons**: Full responsibility for atomicity, consistency, and durability, especially in a catalogue with a high chance
of concurrent writes coupled with stricter requirements for audit/history. Difficulty varies with the tools involved.

#### Triggers

A trigger that intercepts writes to a certain table and injects an insert into the corresponding archive table into the
same transaction. This is practically a customizable native solution, for any storage that allows triggers of that kind.

The limitations and workarounds are similar, too: Trigger context is usually limited to the statement and row data, so
to pass audit metadata to the archive it needs to be included with the catalogue data. The archive storage is the same
as the catalogue's, with an unsurprising overhead on writes, although the archive table can still be used as an outbox
to move its data to external storage (or through logical replication trickery, the burden of executing the trigger
could be shifted to a mirror DB; not that it makes things _simpler or more transparent_).

**Pros**: Broadly available, straightforward, well-known. Serializable ACID OOB, regardless of application architecture.
**Cons**: Limits the pool of tech choices, archive and primary storage are bound in runtime and design. Fairly opaque.

#### Change capture

Another feature that may be available OOB in the database, or at least as a plug-in. In general, CDC is meant to expose
changes to the persistent database as an ordered stream, akin to the WAL, which can then be consumed by the archive (or
DWH, or a search indexer). In practice, the tools, approaches, and interfaces vary and so does the 'mileage' when using
them, both for native and external CDC. The concrete implementation might offer changes without an ordered log, capture
changes by polling the DB (creating potentially business-significant gaps), or just end up being thinly veiled triggers
anyway.

* **Pros**: Great if it fits the expected model and works with minimal investment, providing the necessary tools, data,
and characteristics.
* **Cons**: Usefulness and integration effort varies wildly between databases and even implementations.

#### Command capture

This approach logs the commands issued to the application or the database, not changes actually, factually made. This
still can have value for audit, despite the obvious objections, even if it won't replace an actual content changelog.

The basic design is trivial -- define what data is to be stored, choose a storage, fill it from the source. Adjust
detail and enrich with context details as necessary. The command log may optionally include a top-level outcome of
the command, e.g. for a REST command, it could include the response code, and maybe even the body. Recording _read_
commands \[in full detail, with full retention] is often unnecessary.

A command log can be as simple as a REST server's HTTP request log -- and as complicated as a _journal_ that serves as
the _sole source_ of changes for the entire catalogue. Needless to say, the latter is a foundational element of the
catalogue's design, and not something added just to serve as an audit log.

* **Pros**: Easy to implement, can be completely independent of business applications. Despite tracking limited data,
can still satisfy many audit requests.
* **Cons**: Interpretation of commands can fluctuate with time; serialization often not definitively reproducible.

### Keeping it serial

When examining the history for a specific entity, it's often reasonably important to see it in the order it happened.
Doubly so if the history records incremental updates. Granted, if the catalogue items are not updated very often, or
are updated by a single writer, or have a clear monotonic version provided by CDC or an external source, then a clear
sort order renders the issue moot.

In the less convenient (but hopefully, considerably more rare) case of high-concurrency catalogues with no established
order, measures could be taken to mitigate the uncertainty. There's no need to maintain full order for every change in
the entire catalogue, only for a single entity or every connected entity in a strong-linked composite.  

* Pessimistic locks. Straight and to the point, lock the entity to guarantee it is not modified until all changes and
  audit data is persisted. The _cost_ is non-trivial, in establishing and checking a distributed lock, and in increase
  of complexity when dealing with batch operations. Avoid if at all possible.
* Optimistic locks. Very situational and awkward to use (esp. in batch operations), could be a failsafe if they are
  available and a write-after-reading update must be applied. May be ineffective, if the archives are in a separate
  storage or the common storage is non-transactional.
* Single-writer. If it can be guaranteed -- without locks -- that each entity may only be updated by a single writer
  in the entire cluster, then said writer has full freedom to read, update, write, and archive. For example, pipe all
  write commands / events through a (partitioned) journal / log / source, like, say, Kafka. Complex, with a formidable
  expertise & effort barrier for entry, but a journal core can have many benefits for a distributed multi-component
  system beyond 'hey, free archives'[^4].
* Leave it all to chance. A compromise better suits a roadmap than agonizing over every trivial detail. Should it come
  to pass that the detail is not trivial, and the concurrent writes exceed expectations, breaking history -- solve it
  then. Maybe a little earlier.

As always, there will be other options available, depending on the tools chosen and requirements bequeathed from on
high, but, hopefully, there's enough ideas here to get started.

[^4]: This is turning out to be a bit of a leitmotif, but journals (and, to a degree, unordered messaging tools, too)
are just that interesting and \[can be] that powerful. See Lambda and Kappa architectures and Kafka docs for more.
