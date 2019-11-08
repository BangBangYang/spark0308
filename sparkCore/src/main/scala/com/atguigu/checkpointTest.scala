package com.atguigu

import org.apache.spark.{SparkConf, SparkContext}

object checkpointTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("cp").setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setCheckpointDir("cp")
    val rdd =  sc.makeRDD(Array(1,2,3))
    val maprdd = rdd.map((_,1))
    val result = maprdd.reduceByKey(_+_)
//    result.checkpoint()
    result.foreach(println)
    println(result.toDebugString)


  }
}
