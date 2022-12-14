# publik-feed

Alimentation des utilisateurs et rôles Publik depuis le ldap

## Prérequis
- JDK 11+
- Maven

## Configurer le projet
- Créer et compléter un fichier `application.properties` à la racine du projet sur le modèle de `/src/main/resources/application.sample.properties`

## Configurer et lancer le projet dans Eclipse
- Importer le projet Maven dans eclipse.
- Démarrer l'application en faisant un clic droit sur `fr.univlorraine.publikfeed.Application.java` et en choisissant 'Run As / Java Application'.

## Tâches Maven
- Lancer l'application (hors d'un IDE) :

```
mvn spring-boot:run
```

- Lancer les tests avec docker (recommandé, et docker doit être installé) :

```
mvn -Pdocker-tests verify
```

- Lancer les tests sans docker (chrome doit être installé, et une base mysql doit être disponible telle que configurée dans `src/test/resources/application.properties`) :

```
mvn verify
```

- Créer le package pour production :

```
mvn clean package -Pproduction
```

## Tests d'intégration
### Prérequis
- [Goole Chrome](https://www.google.com/chrome)
ou
- Chrome dans un container Docker :

```
docker run --name chrome -d --shm-size=2g -e START_XVFB=false -p 4444:4444 selenium/standalone-chrome:latest
```

### Tâche maven
- Lancer les tests d'intégration

```
mvn verify
```

## Docker
- Pour construire l'image, buildkit doit être activé:
    - soit en l'activant dans Docker, cf [documentation](https://docs.docker.com/develop/develop-images/#to-enable-buildkit-builds)
    - soit en affectant `1` à la variable d'environnement `DOCKER_BUILDKIT` :
      - linux : `export DOCKER_BUILDKIT=1`
      - windows : `set DOCKER_BUILDKIT=1`


- Construire l'image docker

```
docker build -t publik-feed:latest -t publik-feed:<version> .
```

- Lancer l'application dans un container

```
docker run --name publik-feed -d --restart always -p 8080:8080 -v <chemin complet>/application.properties:/app/application.properties publik-feed:latest
```

## Auteurs

- **[Charlie Dubois](mailto:charlie.dubois@univ-lorraine.fr)** - *Développeur principal* - [Université de Lorraine](http://www.univ-lorraine.fr/)

## Licence

Ce logiciel est régi par la licence [CeCILL](LICENSE).
