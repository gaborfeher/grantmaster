
export function ExampleData1() {
  return {
    "projects": [
      {
        "name": "aaa",
        "incomes": [
          {
            "date": "2015-01-01",
            "foreignAmount":"1000",
            "exchangeRate": "305",
            "localAmount": "305000",
          }
        ],
        "expenses": [
          {
            "date": "2015-02-02",
            "localAmount": "1480",
            "foreignAmount": "4.852459016393442622950819672131",
            "category": "cat1",
          },
          {
            "date": "2015-02-05",
            "localAmount": "990",
            "category": "cat2",
          }
        ],
        "categories": [
          {
            "tagName": "cat1",
          },
          {
            "tagName": "cat2",
          }
        ],
        "foreignCurrency":"USD"
      },
      {
        "name": "bbb",
        "incomes": [],
        "expenses": [],
        "categories": [],
        "foreignCurrency": "EUR"
      }
    ],
    "budgetCategories": {
      "name": "Budget categories",
      "subTags": [
        {
          "name": "cat1",
          "subTags": [],
        },
        {
          "name": "cat2",
          "subTags": [],
        }
      ],
    },
    "currencies": [
      {"name":"HUF"},
      {"name":"USD"},
      {"name":"EUR"}
    ],
    "localCurrency":"HUF"
  }
};

export function ExampleData2() {
  var data = ExampleData1();
  for (var i = 1; i < 1000; ++i) {
    data.projects[0].expenses.push(
      {
        date: '2015-01-' + (i).toString(),
        localAmount: (i + 1).toString(),
        category: 'cat' + (i % 2 + 1).toString()
      }
    );
  }
  return data;
}
