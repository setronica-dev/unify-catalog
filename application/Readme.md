# `application`: The REST API

## Predefined API endpoints:

### SchemaController.java

* `POST /catalog/schema/{category}`

Create or update the JSON validation schema for the provided `category`. The schema is passed in the request body.
Responds with validation results, and code `200` if the update is successful, and `422` if the schema failed validation. 

* `GET /catalog/schema/{category}`

Retrieve the current schema for the provided `category`. Returns the schema in the response body, response code `200`
if the schema is defined, `204` otherwise.

### ProductController.java

* `POST /catalog/product/{identifier}`

Create or update a product with the provided `identifier`. The request body must contain the product's content and
category. Responds with validation results, and code `200` if the update is successful, and `422` if the product's
content failed validation. 

* `GET /catalog/product/{identifier}`

Retrieve the current state of the product with the provided `identifier`. Returns the product DTO in the response body,
response code `200` if the product exists, `204` otherwise.

### ImpexController.java

* `POST /catalog/files/upload`

Upload a CSV product feed for asynchronous import. The request must be a `multipart/form-data` containing one file.
Responds with a token that can be used to monitor the job's progress and status.

The CSV is expected to have the following columns, in any order: `id, category, content`. Other columns are ignored.
 
* `POST /catalog/files/upload`

Start an asynchronous export process for the entire catalogue. Responds with a token that can be used to monitor the
job's progress and status.

The resulting CSV will have the following columns: `id, category, content`.

* `GET /catalog/files/status/{token}`

Retrieve the current status of a job, using a previously acquired token. Available states are `RUNNING`, `COMPLETE`,
`FAILED`, and `UNKNOWN` (if the status does not exist or is otherwise unavailable).

* `GET /catalog/files/stats/{token}`

Retrieve the current statistics of a job, using a previously acquired token. Available stats are `readCount` (total
number of items found in the input data), `writeCount` (fully processed items), and `failedCount` (items that could
not be processed).

* `GET /catalog/files/result/{token}`

Download the final output of a(n export) job, using a previously acquired token. The file will be sent as an attachment.
 
* `DELETE /catalog/files/result/{token}`

Delete the final output of a(n export) job, using a previously acquired token. The file will no longer be downloadable.
