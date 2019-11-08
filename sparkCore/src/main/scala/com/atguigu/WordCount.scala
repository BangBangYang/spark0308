package com.atguigu

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {

  def main(args: Array[String]): Unit = {
    //1、创建配置信息
//    val conf = new SparkConf().setAppName("wc")
    //本地调试
    val conf = new SparkConf().setAppName("wc").setMaster("local")
    //2、创建sparkcontext
    val sc = new SparkContext(conf)
    //3、处理逻辑
       //读数据
//    val lines = sc.textFile(args(0))
    val lines = sc.textFile("C:\\Users\\yangkun\\Desktop\\a.txt")
       //压平flatMap
    val words = lines.flatMap(_.split(" "))

    val k2v = words.map((_,1))
       //reduceByKey(word, x)
    val result = k2v.reduceByKey(_+_)
       //展示输出
//    result.collect()
//    result.saveAsTextFile(args(1))
    result.foreach(println)
    //4、关闭连接
    sc.stop()
  }
}
