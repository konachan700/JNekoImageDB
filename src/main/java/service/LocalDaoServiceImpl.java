package service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.codec.binary.Hex;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import model.ImageEntityWrapper;
import model.Metadata;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import model.entity.TagTypeEntity;
import proto.CryptographyService;
import proto.LocalDaoService;
import proto.UseStorageDirectory;

public class LocalDaoServiceImpl implements UseStorageDirectory, LocalDaoService {

	public static final String FIELD__TAG_TEXT = "tagText";
	public static final String FIELD__IMAGE_HASH = "imageHash";

	public interface DaoServiceTransaction {
		boolean onTransaction(Session s);
	}

	private final String hibernateDatabaseURI;
	private final String hibernateDatabasePassword;

	private SessionFactory hibernateSessionFactory;
	private StandardServiceRegistry hibernateServiceRegistry;
	private Session currentSession = null;

	private final File metadataFile;
	private final MVStore mvStore;
	private final MVMap<byte[], Metadata> imagesMetadata;

	public LocalDaoServiceImpl(CryptographyService cryptographyService) {
		// **************** H2 KV storage ***************
		metadataFile = getFile("metadata.kv");
		mvStore = new MVStore.Builder()
				.fileName(metadataFile.getAbsolutePath())
				.encryptionKey(Hex.encodeHex(cryptographyService.getAuthData()))
				.autoCommitDisabled()
				.cacheSize(64)
				.open();
		imagesMetadata = mvStore.openMap("imagesMetadata");

		// **************** Hibernate ***************
		hibernateDatabaseURI = "jdbc:h2:" + new File(UseStorageDirectory.STORAGE_ROOT_DIR).getAbsolutePath() + File.separator + "database;CIPHER=AES;";
		hibernateDatabasePassword = Hex.encodeHexString(cryptographyService.getAuthData());

		try {
			final Properties prop = new Properties();
			prop.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
			prop.setProperty("hibernate.hbm2ddl.auto", "update");
			prop.setProperty("hibernate.connection.url", hibernateDatabaseURI);
			prop.setProperty("hibernate.connection.username", "mew");
			prop.setProperty("hibernate.connection.password", hibernateDatabasePassword + " " + hibernateDatabasePassword);
			prop.setProperty("dialect", "org.hibernate.dialect.H2Dialect");
			//prop.setProperty("hibernate.show_sql", "true");
			// prop.setProperty("hibernate.format_sql", "true");

			prop.setProperty("connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
			prop.setProperty("hibernate.c3p0.acquire_increment", "1");
			prop.setProperty("hibernate.c3p0.idle_test_period", "30");
			prop.setProperty("hibernate.c3p0.min_size", "1");
			prop.setProperty("hibernate.c3p0.max_size", "2");
			prop.setProperty("hibernate.c3p0.max_statements", "50");
			prop.setProperty("hibernate.c3p0.timeout", "0");
			prop.setProperty("hibernate.c3p0.acquireRetryAttempts", "1");
			prop.setProperty("hibernate.c3p0.acquireRetryDelay", "250");

			final Configuration conf = new Configuration()
					.addProperties(prop)
					.addAnnotatedClass(ImageEntity.class)
					.addAnnotatedClass(TagEntity.class)
					.addAnnotatedClass(TagTypeEntity.class)
					.configure();

			hibernateServiceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(conf.getProperties())
					.build();

			hibernateSessionFactory = conf.buildSessionFactory(hibernateServiceRegistry);
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	/*********************************************** Metadata ********************************************************/

	@Override public synchronized void saveImageMeta(byte[] hash, Metadata metadata) {
		imagesMetadata.put(hash, metadata);
		mvStore.commit();
	}

	@Override public synchronized Metadata getImageMeta(byte[] hash) {
		if (imagesMetadata.containsKey(hash)) {
			return imagesMetadata.get(hash);
		}
		return null;
	}

	/*********************************************** Private hibernate helpers ********************************************************/

	private synchronized void transaction(DaoServiceTransaction daoServiceTransaction) {
		final Session s = getSession();
		s.beginTransaction();
		try {
			if (daoServiceTransaction.onTransaction(s)) {
				s.getTransaction().commit();
				s.clear();
			} else {
				s.getTransaction().rollback();
				s.clear();
				//W("Bad transaction: cancelled by user; thread " + Thread.currentThread().getName());
			}
		} catch (ConstraintViolationException e) {
			s.getTransaction().rollback();
			s.clear();
			//W("Bad transaction: cancelled by " + e.getClass().getSimpleName() + "; thread " + Thread.currentThread().getName() + "; message: " + e.getMessage());
		}
	}

	private synchronized Session getSession() {
		if (currentSession == null) {
			currentSession = hibernateSessionFactory.openSession();
		}

		if (!currentSession.isOpen()) {
			currentSession = hibernateSessionFactory.openSession();
		}

		return currentSession;
	}

	private synchronized <T> Long getCount(Class<T> clazz) {
		final CriteriaBuilder builder = getSession().getCriteriaBuilder();
		final CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		final Root<T> root = criteriaQuery.from(clazz);
		criteriaQuery.select(builder.count(root));
		final Query<Long> query = getSession().createQuery(criteriaQuery);
		final long count = query.getSingleResult();
		return count;
	}

	private synchronized <T> T getUnique(Class<T> clazz, String field, Object value) {
		final CriteriaBuilder builder = getSession().getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(clazz);
		final Root<T> root = query.from(clazz);
		query.select(root).where(builder.equal(root.get(field), value));;
		final Query<T> q = getSession().createQuery(query);
		return q.uniqueResult();
	}

	private synchronized <T> T getAllByList(Class<T> clazz, String field, Collection collection) {
		final CriteriaBuilder builder = getSession().getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(clazz);
		final Root<T> root = query.from(clazz);
		query.select(root).where(builder.in(root.get(field)).value(collection));;
		final Query<T> q = getSession().createQuery(query);
		return q.uniqueResult();
	}

	private synchronized <T> List<T> getAllWithPages(Class<T> clazz, String orderByFieldName, boolean isDesc, int from, int count) {
		final CriteriaBuilder builder = getSession().getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(clazz);
		final Root<T> root = query.from(clazz);
		query.select(root).orderBy(isDesc ? builder.desc(root.get(orderByFieldName)) : builder.asc(root.get(orderByFieldName)));
		final Query<T> q = getSession().createQuery(query).setFirstResult(from).setMaxResults(count);
		return q.getResultList();
	}

	private synchronized <T> List<T> findAllByMask(Class<T> clazz, String field, String mask, int count) {
		final CriteriaBuilder builder = getSession().getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(clazz);
		final Root<T> root = query.from(clazz);
		query.select(root).where(builder.like(root.get(field), mask)).orderBy(builder.asc(root.get(field)));
		final Query<T> q = getSession().createQuery(query).setFirstResult(0).setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public void dispose() {
		hibernateSessionFactory.close();
		StandardServiceRegistryBuilder.destroy(hibernateServiceRegistry);
	}

	/*********************************************** Tags ********************************************************/
	@Override public long tagGetCount() {
		return getCount(TagEntity.class);
	}

	@Override public boolean tagIsExist(String tag) {
		return getUnique(TagEntity.class, FIELD__TAG_TEXT, tag) != null;
	}

	@Override public TagEntity tagGetEntity(String tag) {
		return getUnique(TagEntity.class, FIELD__TAG_TEXT, tag);
	}

	@Override public void tagSave(String tag) {
		if (tag == null || tag.trim().isEmpty()) return;

		final TagEntity tagEntity = getUnique(TagEntity.class, FIELD__TAG_TEXT, tag);
		if (tagEntity != null) {
			tagEntity.setTagText(tag);
			transaction(s -> {
				s.save(tagEntity);
				return true;
			});
		} else {
			addTag(tag);
		}
	}

	private void addTag(String tag) {
		final TagEntity newTagEntity = new TagEntity();
		newTagEntity.setTagText(tag);
		newTagEntity.setTagCreateDate(System.currentTimeMillis());
		newTagEntity.setTagDeleted(false);
		newTagEntity.setTagType(null);
		transaction(s -> {
			s.save(newTagEntity);
			return true;
		});
	}

	@Override public void tagDelete(String tag) {
		if (tag == null || tag.trim().isEmpty()) return;

		final TagEntity tagEntity = getUnique(TagEntity.class, FIELD__TAG_TEXT, tag);
		if (tagEntity != null) {
			tagEntity.setTagDeleted(true);
			getSession().save(tagEntity);
		}
	}

	@Override public List<TagEntity> tagGetEntitiesList(int start, int end) {
		return getAllWithPages(TagEntity.class, FIELD__TAG_TEXT, true, start, end - start);
	}

	@Override public List<TagEntity> tagFindStartedWith(String startWith, int count) {
		return findAllByMask(TagEntity.class, FIELD__TAG_TEXT, startWith + "%", count);
	}

	@Override public List<TagEntity> tagGetOrCreate(final Collection<String> tags) {
		final List<TagEntity> retval = new ArrayList<>();
		tags.forEach(tag -> {
			TagEntity tagEntity = getUnique(TagEntity.class, FIELD__TAG_TEXT, tag);
			if (tagEntity == null) {
				addTag(tag);
				tagEntity = getUnique(TagEntity.class, FIELD__TAG_TEXT, tag);
				if (tagEntity == null) throw new IllegalStateException("WTF");
			}
			retval.add(tagEntity);
		});
		return retval;
	}

	/*********************************************** Image ********************************************************/
	@Override public synchronized void imagesWrite(byte[] imageHash, Collection<TagEntity> tags) {
		if (imageHash == null || imageHash.length <= 0) return;

		final ImageEntity imageEntity = Optional.ofNullable(getUnique(ImageEntity.class, FIELD__IMAGE_HASH, imageHash)).orElse(new ImageEntity());
		imageEntity.setImageHash(imageHash);
		imageEntity.setImageCreateDate(System.currentTimeMillis());
		imageEntity.getTags().addAll(tags);
		transaction(s -> {
			s.save(imageEntity);
			return true;
		});
	}

	@Override public List<ImageEntity> imagesGetHashesList(int start, int end) {
		return getAllWithPages(ImageEntity.class, "imageCreateDate", true, start, end - start);
	}

	@Override public long imagesGetCount() {
		return getCount(ImageEntity.class);
	}

	@Override public boolean imagesElementExist(byte[] imageHash) {
		return getUnique(ImageEntity.class, FIELD__IMAGE_HASH, imageHash) != null;
	}

	@Override public ImageEntityWrapper imagesFindByTags(Collection<TagEntity> tags, int start, int end) {
		if (tags == null || tags.isEmpty()) {
			final List<ImageEntity> images = imagesGetHashesList(start, end);
			final ImageEntityWrapper imageEntityWrapper = new ImageEntityWrapper();
			imageEntityWrapper.setCount((int)imagesGetCount());
			imageEntityWrapper.setList(images);
			return imageEntityWrapper;
		}

		final Set<ImageEntity> images = new HashSet<>();
		tags.stream()
				.map(TagEntity::getImages)
				.forEach(images::addAll);
		ArrayList<ImageEntity> list = new ArrayList<>();
		images.forEach(e -> {
			long count = tags.stream().filter(t -> t.getImages().contains(e)).count();
			if (count == tags.size()) {
				list.add(e);
			}
		});

		list.sort((Comparator.comparingLong(ImageEntity::getImageCreateDate).reversed()));

		final ImageEntityWrapper imageEntityWrapper = new ImageEntityWrapper();
		imageEntityWrapper.setCount(list.size());
		imageEntityWrapper.setList(list.stream().skip(start).limit(end-start).collect(Collectors.toList()));

		return imageEntityWrapper;
	}
}
