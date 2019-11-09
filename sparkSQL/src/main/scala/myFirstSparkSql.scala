import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

object myFirstSparkSql {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("myFirstSparkSql").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().config(conf).getOrCreate()
    //val df = spark.read.json("C:\\Users\\yangkun\\IdeaProjects\\spark0308\\sparkSQL\\src\\main\\scala\\people.json")
    val df = spark.read.json("in/people.json")
    df.show()
    df.select("name").show()
    df.createTempView("people")
    spark.sql("select * from people")

  }
}
