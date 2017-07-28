package club.cookbean.sparrow.cache;


public enum InternalStatus {

    UNINITIALIZED {
        @Override
        public Transition init() {
            return new Transition(AVAILABLE);
        }

        @Override
        public Transition maintenance() {
            return new Transition(MAINTENANCE);
        }

        @Override
        public Status toPublicStatus() {
            return Status.UNINITIALIZED;
        }
    },

    MAINTENANCE {
        @Override
        public Transition close() {
            return new Transition(UNINITIALIZED);
        }

        @Override
        public Status toPublicStatus() {
            return Status.MAINTENANCE;
        }
    },

    AVAILABLE {
        @Override
        public Transition close() {
            return new Transition(UNINITIALIZED);
        }

        @Override
        public Status toPublicStatus() {
            return Status.AVAILABLE;
        }
    },
    ;

    public Transition init() {
        throw new IllegalStateException("Init not supported from " + name());
    }

    public Transition close() {
        throw new IllegalStateException("Close not supported from " + name());
    }

    public Transition maintenance() {
        throw new IllegalStateException("Maintenance not supported from " + name());
    }

    public abstract Status toPublicStatus();

    public class Transition {

        private final InternalStatus to;
        private final Thread owner = Thread.currentThread();

        private volatile InternalStatus done;

        private Transition(final InternalStatus to) {
            if(to == null) {
                throw new NullPointerException();
            }
            this.to = to;
        }

        public InternalStatus get() {
            if(done != null) {
                return done;
            } else if(owner == Thread.currentThread()) {
                return to.compareTo(from()) > 0 ? to : from();
            }
            synchronized (this) {
                boolean interrupted = false;
                try {
                    while(done == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                } finally {
                    if(interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
                return done;
            }
        }

        public synchronized void succeeded() {
            done = to;
            notifyAll();
        }

        public synchronized void failed() {
            done = to.compareTo(from()) > 0 ? from() : to;
            notifyAll();
        }

        public InternalStatus from() {
            return InternalStatus.this;
        }

        public InternalStatus to() {
            return to;
        }

        public boolean done() {
            return done != null;
        }
    }

    public static Transition initial() {
        final Transition close = MAINTENANCE.close();
        close.succeeded();
        return close;
    }

}
