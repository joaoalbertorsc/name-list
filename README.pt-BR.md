# Name List

Uma API REST em Spring Boot para inserir, renomear e excluir nomes. Os nomes ficam em um MySQL que roda em um container
iniciado pelo Docker Compose. A API roda em seu próprio container, gerado a partir do `Dockerfile` deste projeto.

O mesmo nome pode ser gravado mais de uma vez. Renomear ou excluir um nome afeta **todos** os registros com esse nome,
não apenas o primeiro encontrado.

> English version: [README.md](README.md)

## O desafio

| Requisito | Onde encontrar |
|---|---|
| Endpoint para inserir um nome | [`NameController`](src/main/java/com/namelist/controller/NameController.java): `POST /api/v1/names` |
| Endpoint para modificar um nome, atualizando todos os registros com ele | `PUT /api/v1/names/{name}`, com um único `UPDATE` no [`NameRepository`](src/main/java/com/namelist/repository/NameRepository.java) |
| Endpoint para deletar um nome, removendo todos os registros com ele | `DELETE /api/v1/names/{name}`, com um único `DELETE` no [`NameRepository`](src/main/java/com/namelist/repository/NameRepository.java) |
| `docker-compose.yml` com a imagem do MySQL | [`docker-compose.yml`](docker-compose.yml) |
| Dockerfile e imagem da aplicação | [`Dockerfile`](Dockerfile), [passo 2](#passo-a-passo) |
| Banco com `docker compose up` e aplicação com `docker run` | [Passo a passo](#passo-a-passo), passos 1 a 3 |
| API testada no Postman | Coleção em [`postman/`](postman), [passo 4](#passo-a-passo) |

## Tecnologias

Java 21 · Spring Boot 4.1 · Spring Data JPA · MySQL 8.4 · Maven · Docker · Docker Compose · Postman

## Estrutura do projeto

```text
.
├── docker-compose.yml                    # container do MySQL
├── Dockerfile                            # imagem da aplicação
├── pom.xml
├── src/main/java/com/namelist/
│   ├── NameListApplication.java
│   ├── controller/NameController.java    # os endpoints em /api/v1/names
│   ├── model/Name.java                   # entidade JPA mapeada para a tabela names
│   └── repository/NameRepository.java    # repositório Spring Data JPA com a renomeação e exclusão em massa
├── src/main/resources/
│   ├── application.yml
│   └── schema.sql                        # cria a tabela names se ela não existir
└── postman/                              # coleção do Postman
```

## API

| Método | Caminho | Corpo | Resposta |
|---|---|---|---|
| `POST` | `/api/v1/names` | `{"name": "Ana"}` | `201 Created` com o registro gravado: `{"id": 1, "name": "Ana"}` |
| `PUT` | `/api/v1/names/{name}` | `{"name": "Carla"}` | `200 OK` com quantos registros foram renomeados: `{"updated": 3}` |
| `DELETE` | `/api/v1/names/{name}` | nenhum | `200 OK` com quantos registros foram excluídos: `{"deleted": 3}` |
| `GET` | `/api/v1/names` | nenhum | `200 OK` com todos os registros, para conferir o resultado das outras chamadas |

`PUT` e `DELETE` respondem `404 Not Found` quando nenhum registro tem aquele nome. Nomes com espaço vão codificados na
URL, por exemplo `/api/v1/names/Maria%20Clara` (o Postman codifica sozinho).

## Passo a passo

Você só precisa do Docker. Java e Maven rodam dentro do build da imagem.

**1. Suba o banco de dados** com o Docker Compose:

```bash
docker compose up -d --wait
```

Isso inicia o container `name-list-mysql` com o banco `name_list`. O `--wait` só retorna quando o MySQL já aceita
conexões. Os dados ficam no volume `mysql-data` e sobrevivem à recriação do container.

**2. Gere a imagem da aplicação** a partir do `Dockerfile` na raiz do projeto:

```bash
docker build -t name-list .
```

O Dockerfile tem dois estágios. O primeiro compila o jar com Maven e o JDK. O segundo copia só esse jar para uma
imagem menor, apenas com o runtime do Java.

**3. Rode o container da aplicação:**

```bash
docker run -d --name name-list-app --network name-list-network -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/name_list name-list
```

- `--network name-list-network` entra na rede que o Docker Compose criou para o MySQL, então a aplicação encontra o
  banco pelo nome do serviço, `mysql`.
- `-e SPRING_DATASOURCE_URL=...` aponta a aplicação para esse host. Sem isso, ela procuraria o MySQL em `localhost`,
  que dentro de um container é o próprio container.
- `-p 8080:8080` publica a API na porta 8080 da sua máquina.

Confira se os dois containers estão de pé com `docker ps`.

**4. Teste no Postman.** Importe
[`postman/name-list.postman_collection.json`](postman/name-list.postman_collection.json) e rode a coleção inteira
(**Run collection**). Ela insere `Ana` duas vezes e `Bruno` uma vez, renomeia todos os `Ana` para `Carla`, exclui todos
os `Carla` e confere, a cada passo, que todos os registros repetidos foram afetados.

**5. Pare tudo:**

```bash
docker rm -f name-list-app
```

```bash
docker compose down
```

Adicione `-v` ao `docker compose down` para apagar também os nomes gravados.

Para rodar a aplicação fora do container, mantenha o MySQL do passo 1 de pé e rode `./mvnw spring-boot:run`. Os padrões
do [`application.yml`](src/main/resources/application.yml) conectam no MySQL publicado em `localhost:3306`.
