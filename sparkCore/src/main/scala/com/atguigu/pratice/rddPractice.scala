package com.atguigu.pratice

import org.apache.hadoop.conf.Configuration
import org.apache.spark.{SparkConf, SparkContext}

object rddPractice {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("practice").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val logs = sc.textFile("C:\\Users\\yangkun\\IdeaProjects\\spark0308\\sparkCore\\src\\main\\scala\\com\\atguigu\\pratice\\agent.log")
   //将RDD中的string转化为数组RDD[Array[String]]
    val logsArray = logs.map(x => x.split(" "))
    //提取相应的数据，转化粒度RDD[(pro_adId,1)]
    val proAndAd2Count = logsArray.map(x => (x(1) + "_"+ x(4),1))
    //将每个省份每一个广告的所有点击量聚合RDD[(pro_adid, sum)]
    val proAndAd2Sum = proAndAd2Count.reduceByKey((x,y) => x + y )

    //将粒度扩大，拆分key，RDD[(pro,(adid,sum))]
    val pro2AndSum = proAndAd2Sum.map{ x =>
      val param = x._1.split("_")
      (param(0),(param(1),x._2))
    }
    //将每一个省份的广告合并成一个数组[(pro,)]
    val pro2AdArray = pro2AndSum.groupByKey()
    //排序取前三 sortwith(It: (A,A) => Boolean)
    val result = pro2AdArray.mapValues(values =>
      values
            .toList
            .sortWith((x,y) => x._2 > y._2)
            .take(3))
    result.foreach(println)

  }
}
