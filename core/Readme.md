## `core`: An illustration of a catalogue

This is the simplest possible implementation of a catalogue, with free-form JSON documents in a key-value storage, and
with a simple validation system, where the rules are configured as JSON schema. The actual (plus 'expected' or
'potential' or 'budgetary') business requirements will become the puzzle that determines the bounds of the architectural
design of the catalogue, interfaces it should offer, and the technologies that can or should be used. As this
implementation has no business requirements beyond 'validate and store' and 'allow multiple sets of validation rules',
it makes few assumptions and offers even fewer tools to navigate the resulting catalogue.

### Entity

The sole domain entity, `Product`, has an identifier (the key) and JSON content (the value), plus an abstract type, the
'category', that determines _which_ JSON schema will be used to validate the product.

The validation schema are dynamic configuration, rather than entities. They are stored as key-value (with the category
as key and the schema itself as a value) documents as well.

### Public API

* `ProductService`: Exposes read/write access to the persistent collection of `Product`s.
* `ContentValidationService`: Read/write/execute access to the validation schemas for the various product categories.
* `EventService`: Implement this callback interface to receive events when products or schemas are updated.