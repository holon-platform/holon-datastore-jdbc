INSERT INTO test1 VALUES (1, 'One', 7.4, DATE('2016-05-19'), DATE('2016-05-19'), 0, 1, 'n1', 12.65, 's1', null, null, '18:30:15', 'clocbcontent', CAST (X'C9CBBBCCCEB9C8CABCCCCEB9C9CBBB' AS BLOB));
INSERT INTO test1 VALUES (2, 'Two', null, DATE('2016-04-19'), DATE('2016-04-19'), 1, 0, 'n2', 3, 's2', TIMESTAMP('2017-03-23 15:30:25'), TIMESTAMP('2017-03-23 15:30:25'), null, 'clocbcontent', null);

INSERT INTO test3 VALUES (2, 'TestJoin');
INSERT INTO test3 VALUES (3, 'TestJoin3');

INSERT INTO test_recur (name, parent) VALUES ('test1', null);
INSERT INTO test_recur (name, parent) VALUES ('test2', 'test1');
INSERT INTO test_recur (name, parent) VALUES ('test3', 'test2');

INSERT INTO test_nopk (nmb, txt) VALUES (1, 'First');
INSERT INTO test_nopk (nmb, txt) VALUES (2, 'Second');
