create table fa as
	select * from fieldAccess
	where fHashcode not in ('primitive','null','static')

