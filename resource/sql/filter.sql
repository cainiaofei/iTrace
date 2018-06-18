create table fm as
select * from fieldModification group by cSignature, McSignature, fSignature,fHashcode
