create table pp as
	select * from parameterPass
	where fHashcode not in ('primitive','null','static')

