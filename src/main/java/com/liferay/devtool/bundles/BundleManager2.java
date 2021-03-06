package com.liferay.devtool.bundles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.devtool.bundles.reader.BundleDetailsReader;
import com.liferay.devtool.bundles.reader.BundleJarReader;
import com.liferay.devtool.bundles.reader.DbSchemaReader;
import com.liferay.devtool.bundles.reader.PatchingToolReader;
import com.liferay.devtool.process.ProcessEntry;
import com.liferay.devtool.process.WindowsProcessTool;
import com.liferay.devtool.utils.SysEnv;

public class BundleManager2 implements FileSystemScanEventListener {
	private SysEnv sysEnv;
	private BundleEventListener bundleEventListener;
	private List<BundleEntry> bundles = new ArrayList<>();
	private Map<String,BundleEntry> bundleMap = new HashMap<>();
	private List<GitRepoEntry> gitRepos = new ArrayList<>();
	private Map<String,GitRepoEntry> gitRepoMap = new HashMap<>();
	
	public void scanFileSystem() {
		EventBasedFileSystemScanner scanner = new EventBasedFileSystemScanner();
		scanner.setFileSystemScanEventListener(this);
		scanner.scanLocalDisks();
	}

	public void readDetails() {
		if (sysEnv.isWindows()) {
			queryProcessEntries();
		}
		readBundleDetails();
		readBundleVersions();
		readPatchingToolVersions();
		readGitRepoDetails();
		readBundleDbs();
	}

	private void queryProcessEntries() {
		WindowsProcessTool wp = new WindowsProcessTool();
		wp.setSysEnv(sysEnv);
		wp.refresh();

		for (ProcessEntry process : wp.getProcessEntries()) {
			if (process.getBundlePath() != null) {
				if (bundleMap.containsKey(getBundleKey(process.getBundlePath()))) {
					BundleEntry bundle = bundleMap.get(getBundleKey(process.getBundlePath()));
					bundle.setRunningProcess(process);
				}
			}
		}
	}

	private void readBundleDbs() {
		for (BundleEntry bundle : bundles) {
			DbSchemaReader dbSchemaReader = new DbSchemaReader();
			dbSchemaReader.setSysEnv(sysEnv);
			dbSchemaReader.setBundleEntry(bundle);
			dbSchemaReader.readDetails();
			
			sendUpdate(bundle);
		}
	}
	
	private void readBundleVersions() {
		for (BundleEntry bundle : bundles) {
			BundleJarReader bundleJarReader = new BundleJarReader();
			bundleJarReader.setSysEnv(sysEnv);
			bundleJarReader.setBundleEntry(bundle);
			bundleJarReader.readDetails();
			
			sendUpdate(bundle);
		}
	}

	private void readPatchingToolVersions() {
		for (BundleEntry bundle : bundles) {
			PatchingToolReader ptJarReader = new PatchingToolReader();
			ptJarReader.setSysEnv(sysEnv);
			ptJarReader.setBundleEntry(bundle);
			ptJarReader.readDetails();
			
			sendUpdate(bundle);
		}
	}
	
	private void readGitRepoDetails() {
		for (GitRepoEntry gitRepo : gitRepos) {
			GitRepoDetailsReader gitRepoDetailsReader = new GitRepoDetailsReader();
			gitRepoDetailsReader.setSysEnv(sysEnv);
			gitRepoDetailsReader.setGitRepoEntry(gitRepo);
			gitRepoDetailsReader.readDetails();
			
			if (gitRepo.getBuildTargetDir() != null && bundleMap.containsKey(getBundleKey(gitRepo.getBuildTargetDir()))) {
				BundleEntry bundle = bundleMap.get(getBundleKey(gitRepo.getBuildTargetDir()));
				if (bundle.getGitRepos() == null) {
					bundle.setGitRepos(new ArrayList<>());
				}
				
				if (!bundle.getGitRepos().contains(gitRepo)) {
					bundle.getGitRepos().add(gitRepo);
					sendUpdate(bundle);
				}
			}
		}
	}

	private void readBundleDetails() {
		for (BundleEntry bundle : bundles) {
			BundleDetailsReader bundleDetailsReader = new BundleDetailsReader();
			bundleDetailsReader.setSysEnv(sysEnv);
			bundleDetailsReader.setBundleEntry(bundle);
			bundleDetailsReader.readDetails();
			
			sendUpdate(bundle);
		}
	}
	
	private void sendUpdate(BundleEntry bundle) {
		if (bundleEventListener != null) {
			bundleEventListener.onUpdate(bundle);
		}
	}
	
	public SysEnv getSysEnv() {
		return sysEnv;
	}

	public void setSysEnv(SysEnv sysEnv) {
		this.sysEnv = sysEnv;
	}

	public void setBundleEventListener(BundleEventListener bundleEventListener) {
		this.bundleEventListener = bundleEventListener;
	}

	public List<BundleEntry> getEntries() {
		return bundles;
	}

	@Override
	public void onFoundBundle(String absolutePath) {
		if (!bundleMap.containsKey(getBundleKey(absolutePath))) {
			BundleEntry bundleEntry = new BundleEntry();
			bundleEntry.setRootDirPath(absolutePath);
			bundleMap.put(getBundleKey(absolutePath), bundleEntry);
			bundles.add(bundleEntry);
			
			sendUpdate(bundleEntry);
		}
	}

	private String getBundleKey(String bundlePath) {
		return bundlePath.toLowerCase();
	}

	@Override
	public void onFoundGitRepo(String absolutePath) {
		if (!gitRepoMap.containsKey(absolutePath)) {
			GitRepoEntry gitRepoEntry = new GitRepoEntry();
			gitRepoEntry.setRootDir(new File(absolutePath));
			gitRepoMap.put(absolutePath, gitRepoEntry);
			gitRepos.add(gitRepoEntry);
		}
	}
}
