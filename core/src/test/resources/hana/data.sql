INSERT INTO test1 VALUES (1, 'One', 7.4, '2016-05-19', '2016-05-19', 0, 1, 'n1', 12.65, 's1', null, null, '18:30:15', 'clocbcontent', x'C9CBBBCCCEB9C8CABCCCCEB9C9CBBB');
INSERT INTO test1 VALUES (2, 'Two', null, '2016-04-19', '2016-04-19', 1, 0, 'n2', 3, 's2', '2017-03-23 15:30:25', '2017-03-23 15:30:25', null, 'clocbcontent', null);

INSERT INTO test3 VALUES (2, 'TestJoin');
INSERT INTO test3 VALUES (3, 'TestJoin3');

INSERT INTO test_recur (name, parent) VALUES ('test1', null);
INSERT INTO test_recur (name, parent) VALUES ('test2', 'test1');
INSERT INTO test_recur (name, parent) VALUES ('test3', 'test2');

INSERT INTO test_nopk (nmb, txt) VALUES (1, 'First');
INSERT INTO test_nopk (nmb, txt) VALUES (2, 'Second');

COMMIT;