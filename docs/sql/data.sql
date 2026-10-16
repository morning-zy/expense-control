DELETE FROM sys_employee WHERE emp_no = 'E101';
ALTER TABLE sys_employee AUTO_INCREMENT = 11;
SELECT COUNT(*) FROM sys_employee;   -- 应回到 10
