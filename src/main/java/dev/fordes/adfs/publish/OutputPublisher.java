package dev.fordes.adfs.publish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;

import jakarta.inject.Singleton;

import dev.fordes.adfs.error.OutputException;
import dev.fordes.adfs.publish.StagingWorkspace.Workspace;

@Singleton
public final class OutputPublisher {

    /**
     * 以“整目录交换”的方式发布本轮产物。
     *
     * <p>逐文件移动无法保证一批文件的版本一致：中途失败会留下新旧混杂的输出目录，且配置中删除的
     * 输出文件会永久残留。这里改为三步目录级操作：旧目录整体改名为恢复目录、新目录整体进位、
     * 删除恢复目录。任意一步失败都不会产生混合版本；若在两次改名之间中断，下一次运行的
     * {@link StagingWorkspace} 恢复逻辑会把旧目录还原。</p>
     *
     * <p>逐文件校验（路径越界、符号链接、文件集合、大小与 SHA-256）已由
     * {@link PublishManifest#validate} 完成，此处不再重复。</p>
     */
    public void publish(Workspace workspace, PublishManifest manifest) {
        manifest.validate(workspace);
        try {
            StagingWorkspace.verifyStableParent(
                    workspace.parent(), workspace.outputDir(), workspace.previousDir());
            if (Files.exists(workspace.outputDir(), LinkOption.NOFOLLOW_LINKS)) {
                StagingWorkspace.moveAtomic(workspace.outputDir(), workspace.previousDir());
            }
            StagingWorkspace.moveAtomic(workspace.nextDir(), workspace.outputDir());
            TreeDeleter.delete(workspace.previousDir());
        } catch (OutputException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new OutputException(
                    "原子替换输出目录失败, 下次运行会自动恢复: " + workspace.outputDir(), exception);
        }
    }
}
