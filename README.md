This plugin was intended to add quickly ability for centralized logging for the legacy project using SLF4->JUL bridge for logging.
The whole solution looked like:

* Legacy project with SLF4->JUL logging
* https://github.com/mp911de/logstash-gelf - for easy and quick way to send logs from JUL to logstash
* logstash for inputting logs by udp/gelf and output them to elasticsearh
* elasticsearch with the head (http://mobz.github.io/elasticsearch-head/) pluing and this one installed

We assume, that elasticsearch is being used only for logstash and for nothing else.

For this plugin to work as it was intended to, you would need to setup your GELF-JUL log handler. For aggregation,
the 'host' from the documents. But also you would need user 'facility' as it shown below:

biz.paluch.logging.gelf.jul.GelfLogHandler.facility: "YourComponentName"

In this case your logs will be shows as 'folder'->'files' structure, using the 'host' -> YourComponentName.

This plugin is just a quick demo/prototype, so use it on your own risk and you are free to use it and modify
in any way you need it to. This plugin is heavily based on this one: https://github.com/ferhatsb/elasticsearch-log-viewer.git

## How To Install

* Clone this repo
* Inside the cloned folder do: './gradlew clean install'
* Copy the command for intallation produced by Gradle and call it inside elasticsearch HOME folder

## Rest Interfaces

http://localhost:9200/_logs/as_files => lists log files presented as 'folder'->'files' aggregated by 'host'->'facility'

http://localhost:9200/_logs/indexes => list of indexes (first 30) accessible. They can be used in UI for index selection

http://localhost:9200/_logs/{folder}/{file} => returns log as it would return them from file 'folder'->'file'


## UI

http://localhost:9200/_plugin/logs/

