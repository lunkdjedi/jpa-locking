package com.digitaljedi.jpalocking.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

public class LoggingRetryListener extends RetryListenerSupport {

	private Log LOG = LogFactory.getLog(this.getClass());

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
		LOG.debug("Retry close");
		super.close(context, callback, throwable);
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
		LOG.info("Retry onError");
		LOG.info(context);
		super.onError(context, callback, throwable);
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		LOG.debug("Retry open");
		return super.open(context, callback);
	}

}
