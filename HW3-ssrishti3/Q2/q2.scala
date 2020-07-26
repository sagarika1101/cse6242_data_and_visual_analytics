// Databricks notebook source
// Q2 [25 pts]: Analyzing a Large Graph with Spark/Scala on Databricks

// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Definfing the data schema
val customSchema = StructType(Array(StructField("answerer", IntegerType, true), StructField("questioner", IntegerType, true),
    StructField("timestamp", LongType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
// MAKE SURE THAT YOU REPLACE THE examplegraph.csv WITH THE mathoverflow.csv FILE BEFORE SUBMISSION.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "false") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/mathoverflow.csv")
   .withColumn("date", from_unixtime($"timestamp"))
   .drop($"timestamp")

// COMMAND ----------

//display(df)
df.show()

// COMMAND ----------

// PART 1: Remove the pairs where the questioner and the answerer are the same person.
// ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

// ENTER THE CODE BELOW
val df1 = df.filter(!($"answerer" === $"questioner"))
df1.show()

// COMMAND ----------

// PART 2: The top-3 individuals who answered the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest out-degrees.

// ENTER THE CODE BELOW
df1.groupBy($"answerer")
    .count()
    .orderBy(desc("count"),asc("answerer"))
    .limit(3)
    .toDF("answerer","questions_answered")
    .show()


// COMMAND ----------

// PART 3: The top-3 individuals who asked the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest in-degree.

// ENTER THE CODE BELOW
df1.groupBy($"questioner")
    .count()
    .orderBy(desc("count"),asc("questioner"))
    .limit(3)
    .toDF("questioner","questions_asked")
    .show()


// COMMAND ----------

// PART 4: The top-5 most common asker-answerer pairs - sorted in descending order - if tie, the one with lower value node-id in the first column (u->v edge, u value) gets listed first.

// ENTER THE CODE BELOW
df1.groupBy($"answerer",$"questioner")
    .count()
    .orderBy(desc("count"),asc("answerer"),asc("questioner"))
    .limit(5)
    .show()

// COMMAND ----------

// PART 5: Number of interactions (questions asked/answered) over the months of September-2010 to December-2010 (i.e. from September 1, 2010 to December 31, 2010). List the entries by month from September to December.

// Reference: https://www.obstkel.com/blog/spark-sql-date-functions
// Read in the data and extract the month and year from the date column.
// Hint: Check how we extracted the date from the timestamp.

// ENTER THE CODE BELOW
var df2 = df1.filter(df1("date").gt(lit("2010-09-01")) && df1("date").lt("2010-12-31"))

df2.groupBy(month(df2("date")).alias("month"))
    .count()
    .orderBy(asc("month"))
    .toDF("month","total_interactions")
    .show()

// COMMAND ----------

// PART 6: List the top-3 individuals with the maximum overall activity, i.e. total questions asked and questions answered.

// ENTER THE CODE BELOW
var dfAns = df1.groupBy(df1("answerer").alias("user"))
                .count()
                .toDF("user1","ans")
var dfQues = df1.groupBy(df1("questioner").alias("user"))
                .count()
                .toDF("user2","ques")
                
var dfJoin = dfAns.join(dfQues, dfAns.col("user1") === dfQues.col("user2"), "fullouter")   
                  //.show()
var dfWithoutNull = dfJoin.na.fill(0)

var dfNew = dfWithoutNull.withColumn("userID", when($"user1"===0,$"user2").otherwise($"user1"))
                         // .show()
var dfActivity = dfNew.withColumn("total_activity", dfNew("ans") + dfNew("ques"))
                      .orderBy(desc("total_activity"),asc("userID"))
                      .limit(3)
                     // .show()
var finalDF = dfActivity.select("userID","total_activity")
                        .show()

// COMMAND ----------


