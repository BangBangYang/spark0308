package com.atguigu

import org.apache.spark.{SparkConf, SparkContext}

object flatMp {
  def main(args: Array[String]): Unit = {

      val sc = new SparkContext(new SparkConf().setAppName("map_flatMap_demo").setMaster("local"))
      val arrayRDD =sc.parallelize(Array("a_b","c_d","e_f"))
      arrayRDD.foreach(println) //打印结果1
      /*
        a_b
        c_d
        e_f
      */
      arrayRDD.map(s=>{
        s.split("_")
      }).foreach(x=>{
        println(x.mkString(",")) //打印结果2
      })
      /*
      a,b
      c,d
      e,f
       */
      arrayRDD.flatMap(s=>{
        s.split("_")
      }).foreach(x=>{
        println(x.mkString(","))//打印结果3
      })
      /*
      a
      b
      c
      d
      e
      f
       */
  }

}
