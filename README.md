# Name List

A Spring Boot REST API to insert, rename and delete names. The names are stored in MySQL, which runs in a container
started with Docker Compose. The API runs in its own container, built from this project's `Dockerfile`.

The same name can be stored more than once. Renaming or deleting a name affects **every** record with that name, not
only the first one found.

> Versão em português: [README.pt-BR.md](README.pt-BR.md)

## The challenge

| Requirement | Where to find it |
|---|---|
| Endpoint to insert a name | [`NameController`](src/main/java/com/namelist/controller/NameController.java): `POST /api/v1/names` |
| Endpoint to rename a name, updating every record that has it | `PUT /api/v1/names/{name}`, backed by a single `UPDATE` in [`NameRepository`](src/main/java/com/namelist/repository/NameRepository.java) |
| Endpoint to delete a name, removing every record that has it | `DELETE /api/v1/names/{name}`, backed by a single `DELETE` in [`NameRepository`](src/main/java/com/namelist/repository/NameRepository.java) |
| `docker-compose.yml` with the MySQL image | [`docker-compose.yml`](docker-compose.yml) |
| Dockerfile and image of the application | [`Dockerfile`](Dockerfile), [step 2](#step-by-step) |
| Database with `docker compose up` and application with `docker run` | [Step by step](#step-by-step), steps 1 to 3 |
| API tested with Postman | [`postman/`](postman) collection, [step 4](#step-by-step) |

## Tech stack

Java 21 · Spring Boot 4.1 · Spring Data JPA · MySQL 8.4 · Maven · Docker · Docker Compose · Postman

## Project structure

```text
.
├── docker-compose.yml                    # MySQL container
├── Dockerfile                            # application image
├── pom.xml
├── src/main/java/com/namelist/
│   ├── NameListApplication.java
│   ├── controller/NameController.java    # the endpoints under /api/v1/names
│   ├── model/Name.java                   # JPA entity mapped to the names table
│   └── repository/NameRepository.java    # Spring Data JPA repository with the bulk rename and delete
├── src/main/resources/
│   ├── application.yml
│   └── schema.sql                        # creates the names table if it does not exist
└── postman/                              # Postman collection
```

## API

| Method | Path | Body | Response |
|---|---|---|---|
| `POST` | `/api/v1/names` | `{"name": "Ana"}` | `201 Created` with the stored record: `{"id": 1, "name": "Ana"}` |
| `PUT` | `/api/v1/names/{name}` | `{"name": "Carla"}` | `200 OK` with how many records were renamed: `{"updated": 3}` |
| `DELETE` | `/api/v1/names/{name}` | none | `200 OK` with how many records were deleted: `{"deleted": 3}` |
| `GET` | `/api/v1/names` | none | `200 OK` with every record, to check the result of the other calls |

`PUT` and `DELETE` respond `404 Not Found` when no record has that name. Names with spaces go URL-encoded in the
path, for example `/api/v1/names/Maria%20Clara` (Postman encodes them for you).

## Step by step

You only need Docker. Java and Maven run inside the image build.

**1. Start the database** with Docker Compose:

```bash
docker compose up -d --wait
```

This starts the `name-list-mysql` container with the `name_list` database. `--wait` returns once MySQL accepts
connections. The data lives in the `mysql-data` volume, so it survives the container being recreated.

**2. Build the application image** from the `Dockerfile` at the project root:

```bash
docker build -t name-list .
```

The Dockerfile has two stages. The first compiles the jar with Maven and the JDK. The second copies only that jar
into a smaller image with just a Java runtime.

**3. Run the application container:**

```bash
docker run -d --name name-list-app --network name-list-network -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/name_list name-list
```

- `--network name-list-network` joins the network Docker Compose created for MySQL, so the application reaches the
  database by its service name, `mysql`.
- `-e SPRING_DATASOURCE_URL=...` points the application to that host. Without it, it would look for MySQL on
  `localhost`, which inside a container is the container itself.
- `-p 8080:8080` publishes the API on port 8080 of your machine.

Check that both containers are up with `docker ps`.

**4. Test it in Postman.** Import
[`postman/name-list.postman_collection.json`](postman/name-list.postman_collection.json) and run the whole collection
(**Run collection**). It inserts `Ana` twice and `Bruno` once, renames every `Ana` to `Carla`, deletes every `Carla`,
and checks after each step that all the repeated records were affected.

**5. Stop everything:**

```bash
docker rm -f name-list-app
```

```bash
docker compose down
```

Add `-v` to `docker compose down` to also erase the stored names.

To run the application without its container, keep MySQL up from step 1 and run `./mvnw spring-boot:run`. The defaults
in [`application.yml`](src/main/resources/application.yml) connect to the MySQL port published on `localhost:3306`.
