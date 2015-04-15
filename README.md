This repo is the copy of the: https://github.com/ferhatsb/elasticsearch-log-viewer.git
And my attempt to make it work with elasticsearch 1.5.1

## How To Install

* Clone this repo
* Inside the cloned folder do: './gradlew clean install'
* Copy the command for intallation produced by Gradle and call it inside elasticsearch HOME folder

## Rest Interfaces

http://localhost:9200/_logviewer/logs => lists log files of current node

http://localhost:9200/_logviewer/x.log => gets last line of x

http://localhost:9200/_logviewer/x.log?line=10 => gets last 10 lines of x

http://localhost:9200/_logviewer/x.log?type=more => gets first line of x (default type is 'tail')

http://localhost:9200/_logviewer/x.log?type=more&line=10 => gets first 10 lines of x


## UI

http://localhost:9200/_plugin/log-viewer/

![sh](https://raw.github.com/ferhatsb/elasticsearch-log-viewer/master/sh.png)

