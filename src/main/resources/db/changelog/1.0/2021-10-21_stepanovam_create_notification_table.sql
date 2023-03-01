create schema if not exists sys101_notifications;

SET SCHEMA 'sys101_notifications';

CREATE TABLE IF NOT EXISTS notifications
(
  id uuid  PRIMARY KEY,
  event_type text NOT NULL,
  fire_station_sender text,
  content text ,
  created_at timestamptz NOT NULL DEFAULT now() ,
  processed_at timestamptz,
  is_processed boolean NOT NULL DEFAULT false,
  processed_by uuid,
  result text
);
