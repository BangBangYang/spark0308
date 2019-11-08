package com.bupt.sparkStreaming


import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object KafkaSource2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ks")
    val ssc = new StreamingContext(conf, Seconds(5))
    //kafka参数
    val brokers = "hadoop101:9092"
    val zookeeper="hadoop101:2181,hadoop102:2181,hadoop103:2181"
    val sourceTopic = "source"
    val targetTopic = "target"
    val consumerId = "test"
    //封装kafka参数
    val kafkaParams = Map[String, String](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.GROUP_ID_CONFIG -> consumerId
    )

    val kafkaDStream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,Set(sourceTopic))
//    var fromOffsets = Map[TopicPartition, Long]()
//    val messageHandler = (mmd: MessageAndMetadata[String, String])=>(mmd.topic+" "+mmd.partition,mmd.message())
//    val kafkaDStream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder,(String,String)](ssc,kafkaParams,fromOffsets,messageHandler)
    val result = kafkaDStream.flatMap(t => t._2.split(" ")).map((_, 1)).reduceByKey(_+_)
    result.print()

    kafkaDStream.foreachRDD{ rdd =>
      rdd.foreachPartition{ rddPar =>
          //写出到kafka(targetTopic)
          val value = rddPar.map(x => x._2)
          //创建生产者
          val kafkaPool = KafkaPool(brokers)
          val kafkaConn = kafkaPool.borrowObject()
          //生产者发送数据
          for(item <- rddPar){
            kafkaConn.send(targetTopic, item._1,item._2)
          }
          //关闭生产者
        kafkaPool.returnObject(kafkaConn)
      }

    }
/*    var offsetRanges = Array[OffsetRange]()
    kafkaDStream.transform { rdd =>
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }.foreachRDD { rdd =>
      for (o <- offsetRanges) {
        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
      }
    }*/

    ssc.start()
    ssc.awaitTermination()
  }
}
