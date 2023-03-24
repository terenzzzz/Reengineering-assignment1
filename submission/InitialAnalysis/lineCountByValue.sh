# Count every class's line of code with sorted by  line count
# Running Command:  ./lineCountByValue.sh ~/OneDrive/Desktop/jfreechart/src/main/java *.java > lineCountByValue.csv
# Notice: It may take some time to process

while IFS= read -r file; do
  package=$(dirname "$file")
  fileName=$(basename "$file")
  total=$(wc -l < "$file")
  echo "$package,$fileName,$total"
done < <(find "$1" -name "$2") | sort -t',' -k 3nr
