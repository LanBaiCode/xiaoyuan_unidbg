package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("LeoServiceWorker")
public class LeoServiceWorker extends Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private LeoService leoService;

    @Autowired
    public void init(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
    }

    public LeoServiceWorker() {
        super(WorkerPoolFactory.create(LeoServiceWorker::new, Runtime.getRuntime().availableProcessors()));
    }

    public LeoServiceWorker(WorkerPool pool) {
        super(pool);
    }

    @Autowired
    public LeoServiceWorker(UnidbgProperties unidbgProperties,
                            @Value("${spring.task.execution.pool.core-size:4}") int poolSize) {
        super(WorkerPoolFactory.create(LeoServiceWorker::new, Runtime.getRuntime().availableProcessors()));
        this.unidbgProperties = unidbgProperties;
        if (this.unidbgProperties.isAsync()) {
            pool = WorkerPoolFactory.create(pool -> new LeoServiceWorker(unidbgProperties.isDynarmic(),
                unidbgProperties.isVerbose(), pool), Math.max(poolSize, 4));
            log.info("线程池为:{}", Math.max(poolSize, 4));
        } else {
            this.leoService = new LeoService(unidbgProperties);
        }
    }

    public LeoServiceWorker(boolean dynarmic, boolean verbose, WorkerPool pool) {
        super(pool);
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("是否启用动态引擎:{},是否打印详细信息:{}", dynarmic, verbose);
        this.leoService = new LeoService(unidbgProperties);
    }

    @Async
    @SneakyThrows
    public CompletableFuture<String> getSign(String path) {

        LeoServiceWorker worker;
        String data;
        if (this.unidbgProperties.isAsync()) {
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                data = worker.doWork(path);
                pool.release(worker);
                break;
            }
        } else {
            synchronized (this) {
                data = this.doWork(path);
            }
        }
        return CompletableFuture.completedFuture(data);
    }

    private String doWork(String path) {
        return leoService.getSign(path);
    }

    @SneakyThrows
    @Override public void destroy() {
        leoService.destroy();
    }
}
