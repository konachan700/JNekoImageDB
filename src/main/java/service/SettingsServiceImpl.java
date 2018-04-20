package service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import proto.SettingsService;
import proto.UseStorageDirectory;

public class SettingsServiceImpl implements UseStorageDirectory, SettingsService {
	private final File config;
	private final DB db;
	private final Map<UUID, ConcurrentMap> configs = new HashMap<>();

	public SettingsServiceImpl() {
		config = getFile("config.kv");
		db = DBMaker
				.fileDB(config)
				.checksumHeaderBypass()
				.fileMmapEnable()
				.fileChannelEnable()
				.transactionEnable()
				.make();
	}

	@Override
	public <T> T readSettingsEntry(UUID uuid, String name, T defaultValue) {
		if (configs.containsKey(uuid)) {
			final ConcurrentMap map = configs.get(uuid);
			return (T) map.getOrDefault(name, defaultValue);
		} else {
			final ConcurrentMap map = db.hashMap(uuid.toString()).createOrOpen();
			configs.put(uuid, map);
			return (T) map.getOrDefault(name, defaultValue);
		}
	}

	@Override
	public void writeSettingsEntry(UUID uuid, String name, Object value) {
		try {
			if (configs.containsKey(uuid)) {
				configs.get(uuid).put(name, value);
			} else {
				final ConcurrentMap map = db.hashMap(uuid.toString()).createOrOpen();
				map.put(name, value);
				configs.put(uuid, map);
			}
		} finally {
			db.commit();
		}
	}

	@Override public void dispose() {
		db.commit();
		db.close();
	}
}
