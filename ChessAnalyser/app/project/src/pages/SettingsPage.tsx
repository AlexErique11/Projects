import { useState, useEffect } from 'react';
import { Save, Loader2 } from 'lucide-react';
import { supabase } from '../lib/supabase';

const BROWSERS = ['Chrome', 'Brave', 'Firefox', 'Safari', 'Edge', 'Opera'];

export default function SettingsPage() {
  const [elo, setElo] = useState(1500);
  const [defaultBrowser, setDefaultBrowser] = useState('Chrome');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const { data, error } = await supabase
        .from('settings')
        .select('*')
        .maybeSingle();

      if (error) throw error;

      if (data) {
        setElo(data.elo);
        setDefaultBrowser(data.default_browser);
      }
    } catch (error) {
      console.error('Error loading settings:', error);
    } finally {
      setLoading(false);
    }
  };

  const saveSettings = async () => {
    setSaving(true);
    try {
      const { data: existing } = await supabase
        .from('settings')
        .select('id')
        .maybeSingle();

      if (existing) {
        const { error } = await supabase
          .from('settings')
          .update({ elo, default_browser: defaultBrowser })
          .eq('id', existing.id);

        if (error) throw error;
      } else {
        const { error } = await supabase
          .from('settings')
          .insert({ elo, default_browser: defaultBrowser });

        if (error) throw error;
      }
    } catch (error) {
      console.error('Error saving settings:', error);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex-1 bg-gradient-to-br from-orange-50 via-pink-50 to-rose-50 p-8 flex items-center justify-center">
        <Loader2 className="animate-spin text-orange-400" size={40} />
      </div>
    );
  }

  return (
    <div className="flex-1 bg-gradient-to-br from-orange-50 via-pink-50 to-rose-50 p-8">
      <div className="max-w-2xl mx-auto">
        <h2 className="text-3xl font-bold bg-gradient-to-r from-orange-600 to-rose-600 bg-clip-text text-transparent mb-8">Settings</h2>

        <div className="bg-white rounded-lg shadow-lg p-6 space-y-6">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Player ELO Rating
            </label>
            <div className="flex items-center gap-4">
              <input
                type="range"
                min="800"
                max="3000"
                value={elo}
                onChange={(e) => setElo(parseInt(e.target.value))}
                className="flex-1 h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-orange-600"
              />
              <input
                type="number"
                min="800"
                max="3000"
                value={elo}
                onChange={(e) => setElo(parseInt(e.target.value))}
                className="w-24 px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              />
            </div>
            <p className="text-sm text-slate-500 mt-2">
              Set your chess rating (800 - 3000)
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Default Browser
            </label>
            <select
              value={defaultBrowser}
              onChange={(e) => setDefaultBrowser(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            >
              {BROWSERS.map((browser) => (
                <option key={browser} value={browser}>
                  {browser}
                </option>
              ))}
            </select>
            <p className="text-sm text-slate-500 mt-2">
              Choose your preferred browser for opening chess.com
            </p>
          </div>

          <button
            onClick={saveSettings}
            disabled={saving}
            className="w-full py-3 px-4 bg-gradient-to-r from-orange-600 to-rose-600 hover:from-orange-700 hover:to-rose-700 disabled:from-orange-400 disabled:to-rose-400 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2 shadow-lg"
          >
            {saving ? (
              <>
                <Loader2 className="animate-spin" size={18} />
                Saving...
              </>
            ) : (
              <>
                <Save size={18} />
                Save Settings
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
