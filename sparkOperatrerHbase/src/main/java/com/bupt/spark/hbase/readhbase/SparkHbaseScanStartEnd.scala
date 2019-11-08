package com.bupt.spark.hbase.readhbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{TableInputFormat, TableMapReduceUtil}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object SparkHbaseScanStartEnd {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("aa").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val configuration = HBaseConfiguration.create()
    //设置scan对象 限制起止行
    val scan = new Scan()
    scan.addFamily(Bytes.toBytes("info"))
    scan.setBatch(1000)
    scan.setCacheBlocks(false)
    scan.setStartRow(Bytes.toBytes("spark_part"))
    scan.setStopRow(Bytes.toBytes("spark_part|"))
    configuration.set(TableInputFormat.INPUT_TABLE, "student")
    configuration.set(TableInputFormat.SCAN,TableMapReduceUtil.convertScanToString(scan))
    val hbaseRDD = sc.newAPIHadoopRDD(configuration, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result])
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
