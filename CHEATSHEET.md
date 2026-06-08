# Data Structures Review Two — Cheat Sheet

A plain-English guide to every topic in this assignment. Read this top to bottom and
the code in the `exercises` and `homework` folders will make sense line by line.

---

## 0. The mental recipe the course keeps repeating

> Read the problem → Model the data → **Choose the data structure** → Name the operations → Choose the algorithm → Implement.

Most of the grade is just **picking the right container** and then calling the right
method on it. The hard part is matching the *rule of the problem* to a *structure*:

| If the problem says...                  | Use this        | Because                         |
|-----------------------------------------|-----------------|---------------------------------|
| "most recent first", "undo", "back"     | **Stack** (LIFO)| Last In, First Out              |
| "in the order they arrived", "fair line"| **Queue** (FIFO)| First In, First Out             |
| "hand work from one thread to another"  | **BlockingQueue**| Thread-safe + workers can wait |
| "analyze a finished list, answer questions" | **Streams**  | Read-only data crunching        |

---

## 1. Stack (LIFO) — used in `ActionHistory`

A **stack** is a pile of plates. You add to the top (`push`) and remove from the top
(`pop`/`remove`). **L**ast **I**n, **F**irst **O**ut.

```java
Deque<Action> stack = new ArrayDeque<>();
stack.push(a);          // put a on top
stack.push(b);          // put b on top  -> pile is [b, a]
stack.remove();         // takes b off the top (the newest)
stack.peek();           // LOOK at the top (b) without removing
stack.size();           // how many items
stack.isEmpty();        // true if nothing left
```

**Undo/Redo = two stacks.** Undo something → move it from the undo stack to the redo
stack. Do something brand new → the redo stack is no longer valid, so `clear()` it.

---

## 2. Queue (FIFO) — used in `PrintQueue`

A **queue** is a line at a store. Join at the back (`addLast`), get served from the
front (`pollFirst`). **F**irst **I**n, **F**irst **O**ut.

```java
Deque<PrintJob> queue = new ArrayDeque<>();
queue.addLast(job);     // join the back of the line
queue.pollFirst();      // serve + remove the front person (null if empty)
queue.peekFirst();      // LOOK at the front without removing (null if empty)
queue.size();           // how many waiting
```

`ArrayDeque` is a **Deque** ("deck") = double-ended queue. It can act as *either* a
stack or a queue depending on which ends you use. That's why both sections above use it.

---

## 3. `Optional` — "a maybe-empty box"

Returning `null` is dangerous (callers forget to check it and the program crashes).
`Optional` is a safe wrapper that is *either* holding a value *or* explicitly empty.

```java
Optional.of(job)          // a box that definitely holds job
Optional.empty()          // an explicitly empty box
Optional.ofNullable(x)    // empty box if x is null, otherwise a box holding x
```

Pattern used everywhere here: take from a structure (which may give `null`), then
`return Optional.ofNullable(result);`.

---

## 4. Streams — used in the two "Report Builder" files

A **stream** is a temporary conveyor belt over a collection. You chain steps; the
original list is never changed. You assemble it as: **source → middle steps → final step.**

```java
list.stream()                        // 1. SOURCE: start the belt
    .filter(x -> x.score() < 60)     // 2. MIDDLE: keep only items passing the test
    .map(x -> x.name())              //    MIDDLE: transform each item into something else
    .toList();                       // 3. FINAL: collect the survivors into a List
```

### The middle steps you need

| Step                        | What it does                                            |
|-----------------------------|---------------------------------------------------------|
| `.filter(cond)`             | keep only items where `cond` is true                    |
| `.map(x -> ...)`            | turn each item into something else                      |
| `.mapToInt(x -> x.score())` | turn each item into an `int` (unlocks `.sum()`/`.average()`) |

### The final steps ("terminal operations")

| Step                  | Gives you                                                |
|-----------------------|----------------------------------------------------------|
| `.count()`            | how many items (a `long`)                                 |
| `.average()`          | the mean as an `OptionalDouble` → finish with `.orElse(0.0)` |
| `.toList()`           | an **unmodifiable** `List` of the items                  |
| `.collect(groupingBy(...))` | a `Map` that buckets items by a key                |

### Lambdas and method references (the `->` and `::` syntax)

- `x -> x.score() < 60` is a **lambda**: "given an x, return whether its score < 60."
- `SupportTicket::resolved` is a **method reference**: shorthand for
  `t -> t.resolved()`. Both mean the same thing.

### `groupingBy` + `counting` (used for "count by category")

```java
.collect(Collectors.groupingBy(
        t -> t.category(),          // KEY: which bucket each item goes in
        Collectors.counting()))     // VALUE: how many landed in each bucket
// -> { "Billing" -> 2, "Tech" -> 1 }
```

### `DoubleSummaryStatistics` (used in `SensorProcessor`)

A free helper that tracks **count, min, max, sum, average** all at once:

```java
DoubleSummaryStatistics s = new DoubleSummaryStatistics();
s.accept(5.0);  s.accept(10.0);     // feed it numbers one at a time
s.getCount(); s.getMin(); s.getMax(); s.getSum(); s.getAverage();
```

---

## 5. Defensive copies — "don't let outsiders touch my insides"

Several tests check that you can't corrupt an object's data from the outside. Two rules:

1. **Copy data coming IN** (in constructors):
   ```java
   this.tickets = List.copyOf(Objects.requireNonNull(tickets));
   ```
   `requireNonNull` rejects `null`; `List.copyOf` makes a private, frozen copy so if the
   caller later edits *their* list, *ours* is unaffected.

2. **Freeze data going OUT** (in getters): return `Map.copyOf(...)` or `.toList()` so
   callers get a read-only view they cannot `put()` into or `clear()`.

---

## 6. Threading — used in `LogProcessor` and `SensorProcessor`

### The Producer/Consumer pattern

- A **thread** is a worker running code at the same time as other workers.
- **Producer** = code that calls `submit(...)` and drops work on a shared belt.
- **Consumer** = worker threads that take work off the belt and process it.
- The belt is a **`BlockingQueue`**: thread-safe, and a worker asking an empty queue
  will politely *wait* instead of crashing or busy-spinning.

```
producer --submit--> [ BlockingQueue ] --take--> worker 1
                                       --take--> worker 2  (all running in parallel)
```

### Why threads are dangerous, and the 3 tools that make them safe

If two threads change the same plain variable at once, updates get lost. Fixes:

| Tool                    | Use it for                          | Why                                        |
|-------------------------|-------------------------------------|--------------------------------------------|
| `AtomicInteger`         | a shared counter                    | `incrementAndGet()` is one un-splittable step; no `+1` is ever lost |
| `ConcurrentHashMap`     | a shared map of counts              | `merge(key, 1, Integer::sum)` updates safely from many threads |
| `volatile boolean`      | a shared on/off flag                | guarantees every thread *sees* the change immediately |
| `synchronized (obj){ }` | wrapping a non-thread-safe object   | forces threads to take turns inside the block |

### The standard lifecycle (memorize this shape — both files use it)

```java
start(n):   running = true;                       // turn ON before launching workers
            for n times: new Thread(this::workerLoop).start();

workerLoop(): while (running || !queue.isEmpty()) {        // keep going until told to stop AND belt is empty
                  item = queue.poll(100, MILLISECONDS);    // wait up to 100ms for work
                  if (item != null) process(item);         // null just means "nothing yet, loop again"
              }

process(x):  totalProcessed.incrementAndGet();    // safe counting
             ...update shared stats safely...

stop():      running = false;                     // tell workers to wind down
             for each worker: worker.join();      // WAIT for each to fully finish (drains leftover work)
```

The `running || !queue.isEmpty()` condition is the clever part: after `stop()` flips
`running` off, workers keep going *only* until the belt is empty — so no submitted work
is ever dropped. `join()` makes `stop()` block until that's truly done.

---

## 7. Quick file map (what each graded file demonstrates)

| File                       | Topic               | Key structure / tool                    |
|----------------------------|---------------------|-----------------------------------------|
| `ActionHistory`            | Stack / undo-redo   | two `ArrayDeque` stacks                  |
| `PrintQueue`               | Queue / FIFO        | one `ArrayDeque` as a queue              |
| `TicketReportBuilder`      | Streams reporting   | `filter`, `groupingBy`, `average`        |
| `SubmissionReportBuilder`  | Streams reporting   | `filter`, `groupingBy`, `average`        |
| `LogProcessor`             | Threading           | `BlockingQueue`, `AtomicInteger`, `ConcurrentHashMap` |
| `SensorProcessor`          | Threading + stats   | `BlockingQueue`, `synchronized`, `DoubleSummaryStatistics` |

---

## 8. How to run the tests yourself

From the project folder:

```
mvn test
```

Green = `Tests run: 90, Failures: 0, Errors: 0` and `BUILD SUCCESS`. That's the goal.
