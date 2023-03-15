for file in `find $1 -name $2`
do
	total=$(wc -l < $file)
	echo "$file,$total"
done | sort -t',' -k2nr