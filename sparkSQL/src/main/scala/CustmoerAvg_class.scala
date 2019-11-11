import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.sql.expressions.Aggregator


//强类型UDAF
case class userBean(name:String,age:Long)
case class bufferAvg(var sum:Long, var count:Int)
class CustmoerAvg_class extends Aggregator[userBean, bufferAvg, Double]{
  //初始化
  override def zero: bufferAvg =  {
    bufferAvg(0, 0)
  }
//更新缓冲区
  override def reduce(b: bufferAvg, a: userBean): bufferAvg = {
    b.sum += a.age
    b.count += 1
    b
  }
// 合并缓冲区
  override def merge(b1: bufferAvg, b2: bufferAvg): bufferAvg = {
    b1.sum += b2.sum
    b1.count += b2.count
    b1
  }
 //返回值类型
  override def finish(reduction: bufferAvg): Double = {
    reduction.sum.toDouble / reduction.count
  }
 //解码 格式固定
    //自定义的类都这样写
  override def bufferEncoder: Encoder[bufferAvg] = Encoders.product

  override def outputEncoder: Encoder[Double] = Encoders.scalaDouble
}

object testAvg1{
  def main(args: Array[String]): Unit = {
    //0 配置sparkSession
    val conf = new SparkConf().setAppName("myFirstSparkSql").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    val df = spark.read.json("in/user.json")
    val ds = df.as[userBean]
    ds.show()
   //创建聚合对象
    val udaf = new CustmoerAvg_class
    //将聚合函数转化为查询的列
    val avgCol = udaf.toColumn.name("avgAge")
    //应用函数
    ds.select(avgCol).show()
    //释放资源
    spark.close()


  }
}
