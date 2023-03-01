SET SCHEMA 'sys101_notifications';

CREATE TABLE IF NOT EXISTS ext_department
(
  id uuid  PRIMARY KEY,
  short_name text
  );

COMMENT ON TABLE ext_department IS 'Локальная копия части данных таблицы подразделений';