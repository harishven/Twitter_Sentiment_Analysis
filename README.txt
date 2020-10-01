Project located inside Kafka folder
From twitter ddevelopers account get you 
Part 1

	Scala Version	:	2.11.12
	Spark Version	:	2.4.6
	Kafka Version	:	2.12-2.5.0
	ElasticSearch	:	7.8.0
	Kibana			:	7.8.0
	Logstash		:	7.8.0

To run part 1 code :

	*	Create an assembly jar by clicking the assembly in the sbt tasks in intelliJ IDE
	*	Run the following commands in separate command line tabs in the given order inside the kafka installed path:

			**		bin/zookeeper-server-start.sh config/zookeeper.properties
			**		bin/kafka-server-start.sh config/server.properties
			**		bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic topic1

	* Run ElasticSearch in the elasticsearch-7.8.0/bin directory 

			**		./elasticsearch

	* Run Kibana in the kibana-7.8.0-darwin-x86_64/bin directory

			**		./kibana

	* Create a file named logstash-simple.conf in the logstash-7.8.0/ directory and add the following content in it:


					input {
						kafka {
							bootstrap_servers => "localhost:9092"
							topics => ["topic1"]
						}
					}
					output {
						elasticsearch {
							hosts => ["localhost:9200"]
							index => "topic1"
						}
					}

	* Run logstash in the logstash-7.8.0/ directory 

			**		bin/logstash -f logstash-simple.conf

	* Run the assemply jar file in the directory it is present (in my case the home directory)

			**		spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.6 --class TwitterAnalysis Assignment3-assembly-0.1.jar <Consumer Key> <Consumer Secret> <Access Token> <Access Secret> <topic name>

			Note : Give your twitter consumer key, consumer secret, access token and access secret as arguments.
			In <topic name> give "topic1" as the topic we created is topic1.



	* Open http://localhost:5601 and create the elastic search index to visualize the stream data.
