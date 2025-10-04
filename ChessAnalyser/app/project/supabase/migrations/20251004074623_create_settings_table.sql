/*
  # Create Settings Table

  1. New Tables
    - `settings`
      - `id` (uuid, primary key) - Unique identifier for settings record
      - `elo` (integer, not null, default 1500) - Player's ELO rating
      - `default_browser` (text, not null, default 'Chrome') - Default browser preference
      - `created_at` (timestamptz, default now()) - Timestamp when record was created
      - `updated_at` (timestamptz, default now()) - Timestamp when record was last updated

  2. Security
    - Enable RLS on `settings` table
    - Add policy for anonymous users to read settings (public access for demo purposes)
    - Add policy for anonymous users to insert settings
    - Add policy for anonymous users to update settings

  3. Notes
    - This table stores user preferences for the chess application
    - Single settings record pattern - typically one record per user/session
    - ELO range: 800-3000 (enforced by application, can add CHECK constraint if needed)
*/

CREATE TABLE IF NOT EXISTS settings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  elo integer NOT NULL DEFAULT 1500,
  default_browser text NOT NULL DEFAULT 'Chrome',
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

ALTER TABLE settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read access to settings"
  ON settings
  FOR SELECT
  TO anon
  USING (true);

CREATE POLICY "Allow public insert access to settings"
  ON settings
  FOR INSERT
  TO anon
  WITH CHECK (true);

CREATE POLICY "Allow public update access to settings"
  ON settings
  FOR UPDATE
  TO anon
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Allow public delete access to settings"
  ON settings
  FOR DELETE
  TO anon
  USING (true);