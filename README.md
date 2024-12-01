# Permission Service

#### Gestiona la lógica de permisos para editar, leer y compartir snippets Permite a los usuarios compartir snippets con otros usuarios y gestionar qué operaciones están permitidas en cada caso.

### Endpoints del Permission Service
* POST `/permission` 
Crea un nuevo permiso.
* POST `/permission/permissions`
Recupera los permisos asociados a un snippet.
* POST `/permission/snippets/share/{snippetId}`
Comparte un snippet con otro usuario.
* GET `/permission/users`
Lista usuarios para compartir snippets.
* GET `/permission/filetypes`
Obtiene los tipos de archivo soportados.