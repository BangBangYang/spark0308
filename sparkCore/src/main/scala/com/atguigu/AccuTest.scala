package com.atguigu

import org.apache.spark.{SparkConf, SparkContext}

object AccuTest {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("cp").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val arr =  Array(1,2,3,4,5)
    val rdd = sc.makeRDD(arr)
    var sum = sc.accumulator(0)
    rdd.map{x=>
      sum += x
      x
    }.collect
    println(sum.value)


  }
}
