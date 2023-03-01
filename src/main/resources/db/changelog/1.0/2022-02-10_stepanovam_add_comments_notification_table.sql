SET SCHEMA 'sys101_notifications';
COMMENT ON COLUMN  notifications.fire_station_sender  IS 'часть, где зарегистрировано событие';

COMMENT ON COLUMN notifications.processed_at  IS 'когда отработано уведомление';

COMMENT ON COLUMN notifications.processed_by  IS 'кем отработано уведомление';

COMMENT ON COLUMN notifications.result  IS 'результат  отработки уведомления';

COMMENT ON COLUMN notifications.is_processed  IS 'флаг отработки уведомления';

COMMENT ON COLUMN notifications.event_type  IS 'тип события';
