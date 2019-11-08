package com.bupt.sparkStreaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext, kafka}

object KafkaSource {

  def main(args: Array[String]): Unit = {
    //使用SparkStreama完成WordCount
    //spark配置对象
    val conf = new SparkConf().setAppName("wc").setMaster("local[*]");
    //实时数据分析环境对象
    //采集周期，以指定的时间为周期采集实时数据
    val streamingContext = new StreamingContext(conf, Seconds(5));
    //从指定周期的端口采集数据
    //val socketLineStream = streamingContext.socketTextStream("hadoop101",9999);
    val kafkaStream = KafkaUtils.createStream(
      streamingContext,
      "hadoop101:2181",
      "test",
      Map("first" -> 2)
    )
    //采集数据扁平化
    //val wordDStreaming = socketLineStream.flatMap(_.split(" "));
    //kafkaStrem 的返回值时(K,V)类型的数据
    val wordDStreaming =  kafkaStream.flatMap(t => t._2.split(" "));
    //转化结构
    val mapStream = wordDStreaming.map((_,1))
    //统计结果，聚合处理
    val wordToSumDStream = mapStream.reduceByKey(_+_)
    //将结果打印
    wordToSumDStream.print()
    //不能停止采集功能
    //streamingContext.stop()
    //启动采集器
    streamingContext.start()
    //Driver等待执行器的执行
    streamingContext.awaitTermination()

  }

}
