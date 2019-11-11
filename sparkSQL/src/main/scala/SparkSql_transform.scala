import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SparkSql_transform {
  def main(args: Array[String]): Unit = {
    //0 配置sparkSession
    val conf = new SparkConf().setAppName("myFirstSparkSql").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().config(conf).getOrCreate()
    //1 创建rdd
    val rdd = spark.sparkContext.makeRDD(List((1,"yy",23),(2,"yk",21),(3,"yy",25)))
    //2 rdd 转化为df 需要隐式转换
    import spark.implicits._
    val df = rdd.toDF("index","name","age")
    df.show()
    //3 df -> ds
    val ds = df.as[Person]
    //4 ds -> rdd
    val rdd1 = ds.rdd
    rdd1.foreach( row => {
        println(row.name)
    })
    //5 df -> rdd
    val rdd2 = df.rdd
    rdd2.foreach(row => {
      println(row.getString(1))
    })
    //6 rdd -> ds
    val userRDD = rdd.map {
      case (id, name, age) => Person(id, name, age)
    }
    userRDD.toDS().show()
    spark.stop()
    sc.stop()
  }


}
case class Person(index:Int, name:String, age:Int)