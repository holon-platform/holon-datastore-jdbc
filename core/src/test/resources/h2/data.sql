INSERT INTO test1 VALUES (1, 'One', 7.4, parsedatetime('19-05-2016', 'dd-MM-yyyy'), parsedatetime('19-05-2016', 'dd-MM-yyyy'), 0, 1, 'n1', 12.65, 's1', null, null, '18:30:15', 'clocbcontent', x'C9CBBBCCCEB9C8CABCCCCEB9C9CBBB');
INSERT INTO test1 VALUES (2, 'Two', null, parsedatetime('19-04-2016', 'dd-MM-yyyy'), parsedatetime('19-04-2016', 'dd-MM-yyyy'), 1, 0, 'n2', 3, 's2', parsedatetime('23-03-2017 15:30:25', 'dd-MM-yyyy HH:mm:ss'), parsedatetime('23-03-2017 15:30:25', 'dd-MM-yyyy HH:mm:ss'), null, 'clocbcontent', null);

INSERT INTO test3 VALUES (2, 'TestJoin');
INSERT INTO test3 VALUES (3, 'TestJoin3');

INSERT INTO test_recur (name, parent) VALUES ('test1', null);
INSERT INTO test_recur (name, parent) VALUES ('test2', 'test1');
INSERT INTO test_recur (name, parent) VALUES ('test3', 'test2');

commit;
