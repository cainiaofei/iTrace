create table fm as
	select * from fieldModification
	where fHashcode not in ('primitive','null','static')

