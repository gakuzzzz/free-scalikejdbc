ScalikeJDBC **Composable** DSL by Free Monad  [![Build Status](https://travis-ci.org/gakuzzzz/free-scalikejdbc.svg?branch=feature%2Ftest)](https://travis-ci.org/gakuzzzz/free-scalikejdbc)

```scala
  def createProgrammer[F[_]](name: Name, skillIds: List[SkillId])(implicit S: ScalikeJDBC[F], M: Applicative[FreeC[F, ?]]) = {
    import S._
    for {
      id     <- generateKey(insert.into(Programmer).namedValues(pc.name -> name))
      skills <- list(select.from(Skill as s).where.in(s.id, skillIds))(Skill(s))
      _      <- skills.traverse[FreeC[F, ?], Boolean](s => execute(insert.into(ProgrammerSkill).namedValues(sc.programmerId -> id, sc.skillId -> s.id)))
    } yield Programmer(id, name, skills)
  }
```

```scala
  val newProgrammer = DB.localTx {
    Interpreter.transaction.run(createProgrammer("Alice", List(2, 3)))
  }
```