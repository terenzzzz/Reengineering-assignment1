# Count every class's line of code with sorted by package
# Running Command:  ./lineCountByPackage.sh ~/OneDrive/Desktop/jfreechart/src/main/java *.java > lineCountWithSort.csv
# Notice: It may take some time to process

while IFS= read -r file; do
  package=$(dirname "$file")
  fileName=$(basename "$file")
  total=$(wc -l < "$file")
  echo "$package,$fileName,$total"
done < <(find "$1" -name "$2") | sort -t',' -k 1,1 -k 3n |
awk -F, 'BEGIN {package=""}
         {if ($1!=package) {print ""; package=$1} print}'