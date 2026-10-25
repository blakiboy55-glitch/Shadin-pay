const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("Unauthenticated user cannot read or write user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.doc("users/user_123").get());
  await assertFails(
    unauthDb.doc("users/user_123").set({
      userId: "user_123",
      fullName: "Rahim Ahmed",
      phoneNumber: "+8801711223344",
      role: "customer",
      status: "active",
      kycStatus: "unverified",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user can create and read their own profile", async () => {
  const authDb = testEnv.authenticatedContext("user_123").firestore();
  await assertSucceeds(
    authDb.doc("users/user_123").set({
      userId: "user_123",
      fullName: "Rahim Ahmed",
      phoneNumber: "+8801711223344",
      role: "customer",
      status: "active",
      kycStatus: "unverified",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(authDb.doc("users/user_123").get());
});

test("Authenticated user cannot modify another user's profile", async () => {
  const authDb = testEnv.authenticatedContext("user_456").firestore();
  await assertFails(
    authDb.doc("users/user_123").set({
      userId: "user_123",
      fullName: "Hacker",
      phoneNumber: "+8801711223344",
      role: "customer",
      status: "active",
      kycStatus: "unverified",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Wallet access is restricted to owner", async () => {
  const user1Db = testEnv.authenticatedContext("user_123").firestore();
  const user2Db = testEnv.authenticatedContext("user_456").firestore();

  await assertSucceeds(
    user1Db.doc("wallets/user_123").set({
      walletId: "user_123",
      userId: "user_123",
      balance: 5000,
      currency: "BDT",
      dailyLimit: 25000,
      monthlyLimit: 100000,
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(user1Db.doc("wallets/user_123").get());
  await assertFails(user2Db.doc("wallets/user_123").get());
});

test("Transactions ledger is append-only and cannot be deleted", async () => {
  const user1Db = testEnv.authenticatedContext("user_123").firestore();

  await assertSucceeds(
    user1Db.doc("transactions/tx_999").set({
      transactionId: "tx_999",
      senderId: "user_123",
      recipientName: "Karim",
      type: "SEND_MONEY",
      amount: 500,
      fee: 5,
      totalAmount: 505,
      status: "SUCCESS",
      createdAt: new Date(),
    })
  );

  // Financial ledger is immutable - deletes must fail
  await assertFails(user1Db.doc("transactions/tx_999").delete());
});
