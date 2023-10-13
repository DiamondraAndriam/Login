# Gestion session pour load balancing

## A propos

Il s'agit ici de stocker les données en session dans une base de données.
On le met alors les sessions dans l'objet PgHttpSession

Contructeur: Crée automatiquement la session dans la base de données

``PgHttpSession pg = new PgHttpSession(request.getHttpSession());``

PS: n'oubliez pas de configuer la base de données dans la fonction Util.connect()

Ajouter un nouvel élément de session:
``pg.setAttribute("nom de l'attribut", valeurAttribut);``

Ajouter plusieurs sessions à la fois sous forme de HashMap<String,Object>:
``pg.setAttributes(hashmap);``

Modifier les valeurs de session à la fois avec un HashMap<String,Object>:
``pg.setChangedAttributes(hashmap);``

Supprimer un attribut:
``pg.removeAttribute("nom de l'attribut");``
