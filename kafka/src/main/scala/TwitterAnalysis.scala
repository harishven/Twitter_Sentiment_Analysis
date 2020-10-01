
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.log4j.{Level, Logger}

object TwitterAnalysis {

  def main(args: Array[String]): Unit = {
    if (args.length < 5) {
      System.err.println("Usage: TwitterHashTagJoinSentiments <consumer key> <consumer secret> " +
        "<access token> <access token secret> <topic>")
      System.exit(1)
    }

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret, topic) = args.take(5)
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)
    val filters = Seq("covid19") //args.takeRight(args.length - 4) -- using filter for COVID19, you can use any topic.

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.WARN)

    val sparkConf = new SparkConf().setAppName("TwitterSentimentAnalysis")
    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[2]")
    }
    val sparkContext = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sparkContext, Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None, filters)

    val tweetText = stream.filter(_.getLang() == "en").map(status => (status.getText))
    tweetText.foreachRDD{(rdd, time) =>
      rdd.foreachPartition {partition =>
        val props = new Properties()
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("bootstrap.servers", "localhost:9092")

        val producer = new KafkaProducer[String, String](props)

        partition.foreach{ status =>
          val sentiment = SentimentAnalyzer.mainSentiment(status)
          val data = new ProducerRecord[String, String](topic, "Sentiment", status + " : " + sentiment)
          producer.send(data)
        }

        producer.flush()
        producer.close()
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }

}
