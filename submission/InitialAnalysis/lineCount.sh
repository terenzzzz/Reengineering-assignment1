# Count every class's line of code
# Running Command: ./lineCount.sh ~/OneDrive/Desktop/jfreechart/src/main/java *.java > lineCount.csv
# Notice: It may take some time to process

for file in `find $1 -name $2`
do
	total=$(wc -l < $file)
	echo "$file,$total"
done