package com.bupt.spark.hbase.readhbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{TableInputFormat, TableOutputFormat}
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object SparkHbaseScan {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("aa").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val configuration = HBaseConfiguration.create()
    configuration.set(TableInputFormat.INPUT_TABLE, "student")
    val hbaseRDD = sc.newAPIHadoopRDD(configuration,classOf[TableInputFormat],classOf[ImmutableBytesWritable],classOf[Result])
    println(hbaseRDD.getNumPartitions)
    hbaseRDD.foreach(t => {
      val cf = Bytes.toBytes("info")
      val cn = Bytes.toBytes("count")
      val rowkey = Bytes.toString(t._1.get())
      val count = Bytes.toString(t._2.getValue(cf, cn))
      println(s"rowKey:${rowkey},count:${count}")

    })

  }

}
