package com.bupt.sparkStreaming

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}

object KafkaDirect_ZK_Offset {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    val conf: SparkConf = new SparkConf().setAppName("KafkaDirect_ZK_Offset").setMaster("local[*]")
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
    val groupId = "offsetTest"
    val brokers = "hadoop101:9092"
    /**
      * kafka参数列表
      */
    val kafkaParams = Map[String, String](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.GROUP_ID_CONFIG -> groupId
    )

    val topic = "offsettest"
    // val topics = ArrayList(topic)


    val zKGroupTopicDirs: ZKGroupTopicDirs = new ZKGroupTopicDirs(groupId, topic)
    /**
      * 生成的目录结构
      * /customer/offsetTest/offsets/offsettest
      */

    val offsetDir: String = zKGroupTopicDirs.consumerOffsetDir
    //zk字符串连接组
    // val zkGroups = "hadoop10l:2181,hadoop102:2181"
    val zkGroups = "192.168.1.102:2181"
    println(offsetDir)
    //创建一个zkClient连接
    val zkClient: ZkClient = new ZkClient(zkGroups)
    //子节点的数量
    val childrenCount: Int = zkClient.countChildren(offsetDir)

    //子节点的数量>0就说明非第一次
    var stream: InputDStream[(String, String)] = null
    if (childrenCount > 0) {
      println("已经启动过")
      //用来存储我们已经读取到的偏移量
      var fromOffset = Map[TopicAndPartition, Long]()
      (0 until childrenCount).foreach(partitionId => {
        val offset = zkClient.readData[String](offsetDir + s"/$partitionId")
        fromOffset += (new TopicAndPartition(topic, partitionId) -> offset.toLong)
      })
      val mess = (mmd: MessageAndMetadata[String, String])=>(mmd.key(),mmd.message()) //这个会将 kafka 的消息进行 transform，最终 kafka的数据都会变成 (topic_name, message) 这样的 tuple
      stream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder,(String,String)](ssc, kafkaParams,fromOffset,mess)
/*      for (tp <- fromOffset) {
        println(s"topAndPartition: ${tp._1.toString}  offset: ${tp._2}")
      }*/
    }
    else {
      println("第一次启动")
      stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, Set(topic))

    }
    val flatMapStream = stream.flatMap(t => t._2.split(" "))
    val mapWithStateDStream = flatMapStream.map((_,1))
    var result = mapWithStateDStream.reduceByKey(_+_)
   // result.print()
    stream.foreachRDD(
      rdd => {
        //转换rdd为Array[OffsetRange]
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        //计算逻辑

//        rdd.foreach(println)
//        rdd.partitions.foreach(println)
//        rdd.values.foreach(println)
        //自己存储数据，自己管理
        for (o <- offsetRanges) {
          //写入到zookeeper,第二个参数为是否启动安全
          println(s"topicAndPartition: ${o.topicAndPartition()} top: ${o.topic} patition: ${o.partition} utiloffset: ${o.untilOffset} fromoffset: ${o.fromOffset}")
          val path = offsetDir +"/"+o.partition
       //   println(path)
          ZkUtils.updatePersistentPath(zkClient,path,o.untilOffset.toString)
        }
      }

    )
    ssc.start()
    ssc.awaitTermination()
  }
}
