package com.bupt.sparkStreaming

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object test {
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

    /**
      * 如果我们自己维护偏移量
      * 问题：
      * 1.程序在第一次启动的时候，应该从什么开始消费数据？earliest
      * 2.程序如果不是第一次启动的话，应该从什么位置开始消费数据？
      * 上一次自己维护的偏移量接着往后消费，比如上一次存储的offset=88
      */
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
    //KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,Set(topic))
    //子节点的数量>0就说明非第一次

    var stream: InputDStream[(String, String)] = null
    if (childrenCount > 0) {
      println("已经启动过")
      //用来存储我们已经读取到的偏移量
      var fromOffset:Map[TopicAndPartition, Long] = Map()
      var fromtest: Map[TopicAndPartition, Long] = Map()
      fromtest += (new TopicAndPartition("a", 1) -> 100.toLong)
      fromtest += (new TopicAndPartition("b", 1) -> 100.toLong)
      (0 until childrenCount).foreach(partitionId => {
        val offset = zkClient.readData[String](offsetDir + s"/$partitionId")
        fromOffset += (new TopicAndPartition(topic, partitionId) -> offset.toLong)
        fromtest += (new TopicAndPartition(topic, partitionId) -> offset.toLong)
      })
      val mes = (mmd: MessageAndMetadata[String, String]) => (mmd.key, mmd.message)
      stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams,fromtest , mes)
    }
    else {
      println("第一次启动")
      stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, Set(topic))

    }

     stream.foreachRDD(
    rdd => {
      //转换rdd为Array[OffsetRange]
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      //val maped: RDD[(String, String)] = rdd.map(record => (record.key,record.value))
      //计算逻辑
      // maped.foreach(println)
      //自己存储数据，自己管理
      for (o <- offsetRanges) {
        //写入到zookeeper,第二个参数为是否启动安全
        println(s"topicAndPartition: ${o.topicAndPartition()} top: ${o.topic} patition: ${o.partition} utiloffset: ${o.untilOffset} fromoffset: ${o.fromOffset}")
        val path = offsetDir +"/"+o.partition
        println(path)
        ZkUtils.updatePersistentPath(zkClient,path,o.untilOffset.toString)
      }
    }

  )
    ssc.start()
    ssc.awaitTermination()
  }

}
