create table test1 (
     keycode numeric(20) 
    ,strv varchar(100) 
    ,decv numeric(16,6)  
    ,datv date 
    ,datv2 date 
    ,enmv int 
    ,nbv int not null
    ,nst1 varchar(10) default 'nst1'
    ,nst2 numeric(14,4)
    ,nss1 varchar(10) 
    ,tms timestamp
    ,tms2 timestamp
    ,tm timestamp
    ,clb clob
    ,blb blob
    ,primary key (keycode)
);

create table test3 (
     code numeric(20) not null
    ,text varchar(100) not null
    ,primary key (code)
);

create table test_recur (
	 code numeric(20) 
	,name varchar(100) not null
	,parent varchar(100)
);

create table test_nopk (
	nmb numeric(10),
	txt varchar(10)
)
